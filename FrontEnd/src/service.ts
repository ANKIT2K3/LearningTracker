import type { Account, Activity, Assignment, Credentials, Database, Learner, Status, User, Workspace } from './types';
import { createSeed } from './seed';

const DATA_KEY = 'learnflow.workspace.v1';
const SESSION_KEY = 'learnflow.session.v1';
const API_BASE = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') || '';
export const isDemo = import.meta.env.MODE === 'test' || import.meta.env.VITE_DEMO === 'true';
export const STATUSES: Status[] = ['Not Started', 'In Progress', 'Completed'];
const fail = (message: string): never => { throw new Error(message); };
const uid = () => crypto.randomUUID();

export function validateActivity(input: Omit<Activity, 'id'>) {
  if (!input.title.trim() || !input.topic.trim()) fail('Enter a title and topic.');
  if (!Number.isFinite(input.plannedHours) || input.plannedHours <= 0) fail('Planned hours must be greater than zero.');
}
export function validateLearner(input: Omit<Learner, 'id'>, learners: Learner[]) {
  if (!input.name.trim() || !input.cohort.trim()) fail('Enter a name and cohort.');
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(input.email.trim())) fail('Enter a valid email address.');
  if (learners.some(l => l.email.toLowerCase() === input.email.trim().toLowerCase())) fail('A candidate with this email already exists.');
}
export function validateAssignment(learnerId: string, activityId: string, db: Workspace) {
  if (!db.learners.some(l => l.id === learnerId) || !db.activities.some(a => a.id === activityId)) fail('Select an existing learner and activity.');
  if (db.assignments.some(a => a.learnerId === learnerId && a.activityId === activityId)) fail('This activity is already assigned to this learner.');
}
export function summary(assignments: Assignment[]) {
  const completed = assignments.filter(a => a.status === 'Completed').length;
  return { total: assignments.length, completed, inProgress: assignments.filter(a => a.status === 'In Progress').length, notStarted: assignments.filter(a => a.status === 'Not Started').length, percent: assignments.length ? Math.round(completed / assignments.length * 100) : 0 };
}
function read(): Database {
  const raw = localStorage.getItem(DATA_KEY);
  if (!raw) { const seed = createSeed(); write(seed); return seed; }
  try {
    const db = JSON.parse(raw) as Database;
    if (![db.learners, db.activities, db.assignments, db.accounts].every(Array.isArray)) throw new Error();
    return db;
  } catch { return fail("Saved demo data could not be read. Clear this site's storage to start a fresh demo."); }
}
function write(db: Database) {
  try { localStorage.setItem(DATA_KEY, JSON.stringify(db)); }
  catch { fail('Changes could not be saved. Check that browser storage is enabled and has space, then try again.'); }
}
function saveSession(user: User) { sessionStorage.setItem(SESSION_KEY, JSON.stringify(user)); return user; }
export function getSession(): User | null {
  try { const value = sessionStorage.getItem(SESSION_KEY); return value ? JSON.parse(value) as User : null; }
  catch { sessionStorage.removeItem(SESSION_KEY); return null; }
}
function authorize(role?: 'Coordinator' | 'Candidate', allowSetup = false): User {
  const user = getSession();
  if (!user || user.expiresAt <= Date.now()) { sessionStorage.removeItem(SESSION_KEY); return fail('Your session has expired. Please sign in again.'); }
  const account = read().accounts.find(a => a.learnerId === user.id);
  if (user.role === 'Candidate' && (!account || account.version !== user.version)) { sessionStorage.removeItem(SESSION_KEY); return fail('Your session has ended. Sign in with your latest credentials.'); }
  if (role && role !== user.role) fail('You do not have permission to perform this action.');
  if (user.mustChangePassword && !allowSetup) fail('Set your new password before accessing training.');
  return user;
}

// ── API shape returned by the backend ────────────────────────────────────────
// The backend AssignmentResponse: { id: number, learnerId: number, activity: ActivityResponse, status: string }
interface ApiAssignmentResponse {
  id: number;
  learnerId: number;
  activity: Activity & { id: number };
  status: Status;
}
interface ApiLearnerResponse {
  id: number;
  email: string;
  name: string;
  cohort: string;
  mustChangePassword: boolean;
}
interface ApiLoginResponse {
  id: number;
  name: string;
  email: string;
  role: 'Coordinator' | 'Candidate';
  mustChangePassword: boolean;
  expiresAt: number;
  version: number;
}

function mapLearner(l: ApiLearnerResponse): Learner {
  return { id: String(l.id), name: l.name, email: l.email, cohort: l.cohort };
}
function mapAssignment(a: ApiAssignmentResponse): Assignment {
  return { id: String(a.id), learnerId: String(a.learnerId), activityId: String(a.activity.id), status: a.status };
}
function mapActivity(a: ApiAssignmentResponse['activity']): Activity {
  return { id: String(a.id), title: a.title, topic: a.topic, plannedHours: a.plannedHours };
}
// ─────────────────────────────────────────────────────────────────────────────

async function request<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  let response: Response;
  // In dev, Vite proxies /api → localhost:8081 so we just use relative paths.
  // In production VITE_API_BASE_URL is set to the Render backend URL.
  const base = API_BASE ?? '';
  try { response = await fetch(`${base}/api/v1${path}`, { method, credentials: 'include', headers: body ? { 'Content-Type': 'application/json' } : undefined, body: body ? JSON.stringify(body) : undefined }); }
  catch { return fail('Cannot reach the server. Check your connection and try again.'); }
  if (!response.ok) {
    if (response.status === 401) sessionStorage.removeItem(SESSION_KEY);
    const error = await response.json().catch(() => ({}));
    return fail(response.status === 401 ? 'Your session has expired or your credentials are invalid. Please sign in again.' : error.message || 'The request could not be saved. Please try again.');
  }
  return response.status === 204 ? undefined as T : response.json();
}
async function hashPassword(password: string, salt: string) {
  const key = await crypto.subtle.importKey('raw', new TextEncoder().encode(password), 'PBKDF2', false, ['deriveBits']);
  const hash = await crypto.subtle.deriveBits({ name: 'PBKDF2', salt: new TextEncoder().encode(salt), iterations: 100000, hash: 'SHA-256' }, key, 256);
  return Array.from(new Uint8Array(hash)).map(n => n.toString(16).padStart(2, '0')).join('');
}
async function newAccount(learnerId: string, version = 1): Promise<{ account: Account; password: string }> {
  const password = `Lf!${crypto.randomUUID().replace(/-/g, '').slice(0, 13)}`;
  const salt = uid();
  return { password, account: { learnerId, salt, hash: await hashPassword(password, salt), mustChangePassword: true, expiresAt: Date.now() + 86400000, version } };
}
export const service = {
  async demo(role: 'Coordinator' | 'Candidate'): Promise<User> {
    if (!isDemo) return fail('Demo access is unavailable in API mode.');
    const db = read();
    if (role === 'Coordinator') return saveSession({ id: 'coordinator', name: 'Alex Taylor', email: 'coordinator@example.com', role, mustChangePassword: false, version: 1, expiresAt: Date.now() + 28800000 });
    let account = db.accounts.find(a => a.learnerId === 'jordan');
    if (!account) {
      account = (await newAccount('jordan')).account;
      account.mustChangePassword = false; db.accounts.push(account); write(db);
    }
    const learner = db.learners.find(l => l.id === 'jordan')!;
    return saveSession({ ...learner, role, mustChangePassword: account.mustChangePassword, version: account.version, expiresAt: Date.now() + 28800000 });
  },
  async login(email: string, password: string): Promise<User> {
    if (!isDemo) {
      const res = await request<ApiLoginResponse>('/auth/login', 'POST', { email, password });
      return saveSession({ id: String(res.id), name: res.name, email: res.email, role: res.role, mustChangePassword: res.mustChangePassword, version: res.version, expiresAt: res.expiresAt });
    }
    const db = read();
    const learner = db.learners.find(l => l.email === email.trim().toLowerCase());
    const account = db.accounts.find(a => a.learnerId === learner?.id);
    if (!learner || !account || (account.mustChangePassword && account.expiresAt < Date.now()) || await hashPassword(password, account.salt) !== account.hash) return fail('Unable to sign in. Check your credentials or ask your coordinator for a reset.');
    return saveSession({ ...learner, role: 'Candidate', mustChangePassword: account.mustChangePassword, version: account.version, expiresAt: Date.now() + 28800000 });
  },
  async logout() { if (!isDemo) await request('/auth/logout', 'POST'); sessionStorage.removeItem(SESSION_KEY); },
  async workspace(): Promise<Workspace> {
    if (!isDemo) {
      const user = getSession() || fail('Please sign in.');
      if (user.role === 'Candidate') {
        // GET /me/assignments → AssignmentResponse[] with nested activity
        const enriched = await request<ApiAssignmentResponse[]>('/me/assignments');
        const activities = [...new Map(enriched.map(a => [a.activity.id, mapActivity(a.activity)])).values()];
        const assignments = enriched.map(mapAssignment);
        const learnerId = enriched.length > 0 ? String(enriched[0].learnerId) : user.id;
        return { learners: [{ id: learnerId, name: user.name, email: user.email, cohort: '' }], assignments, activities };
      }
      const apiLearners = await request<ApiLearnerResponse[]>('/learners');
      const apiActivities = await request<Activity[]>('/activities');  // ActivityResponse: {id, title, topic, plannedHours}
      const learners = apiLearners.map(mapLearner);
      // Activities from backend have numeric ids, convert to string
      const activities: Activity[] = (apiActivities as (Activity & { id: number | string })[]).map(a => ({ ...a, id: String(a.id) }));
      const rawAssignments = (await Promise.all(
        apiLearners.map(l => request<ApiAssignmentResponse[]>(`/learners/${l.id}/assignments`))
      )).flat();
      const assignments = rawAssignments.map(mapAssignment);
      return { learners, activities, assignments };
    }
    const user = authorize(); const db = read();
    if (user.role === 'Coordinator') return { learners: db.learners, activities: db.activities, assignments: db.assignments };
    const assignments = db.assignments.filter(a => a.learnerId === user.id);
    return { learners: db.learners.filter(l => l.id === user.id), assignments, activities: db.activities.filter(a => assignments.some(x => x.activityId === a.id)) };
  },
  async saveActivity(input: Omit<Activity, 'id'>, id?: string) {
    validateActivity(input);
    if (!isDemo) {
      const res = await request<Activity & { id: number }>(`/activities${id ? `/${id}` : ''}`, id ? 'PUT' : 'POST', { title: input.title, topic: input.topic, plannedHours: input.plannedHours });
      return { ...res, id: String(res.id) };
    }
    authorize('Coordinator'); const db = read();
    const activity = { ...input, title: input.title.trim(), topic: input.topic.trim(), id: id || uid() };
    if (id && !db.activities.some(a => a.id === id)) fail('Activity no longer exists.');
    db.activities = id ? db.activities.map(a => a.id === id ? activity : a) : [...db.activities, activity]; write(db);
    return activity;
  },
  async addLearner(input: Omit<Learner, 'id'>): Promise<Credentials> {
    if (!isDemo) {
      // POST /learners → { learner, temporaryPassword, email, expiresAt }
      const res = await request<{ email: string; temporaryPassword: string; expiresAt: number }>('/learners', 'POST', { name: input.name, email: input.email, cohort: input.cohort });
      return { email: res.email, temporaryPassword: res.temporaryPassword, expiresAt: res.expiresAt };
    }
    authorize('Coordinator'); const db = read(); validateLearner(input, db.learners);
    const learner = { id: uid(), name: input.name.trim(), email: input.email.trim().toLowerCase(), cohort: input.cohort.trim() };
    const { account, password } = await newAccount(learner.id);
    db.learners.push(learner); db.accounts.push(account); write(db);
    return { email: learner.email, temporaryPassword: password, expiresAt: account.expiresAt };
  },
  async resetPassword(id: string): Promise<Credentials> {
    if (!isDemo) {
      // POST /learners/{id}/reset-password → { email, temporaryPassword, expiresAt }
      const res = await request<Credentials>(`/learners/${id}/reset-password`, 'POST');
      return res;
    }
    authorize('Coordinator'); const db = read(); const learner = db.learners.find(l => l.id === id) || fail('Candidate not found.');
    const old = db.accounts.find(a => a.learnerId === id);
    const { account, password } = await newAccount(id, (old?.version || 0) + 1);
    db.accounts = [...db.accounts.filter(a => a.learnerId !== id), account]; write(db);
    return { email: learner.email, temporaryPassword: password, expiresAt: account.expiresAt };
  },
  async assign(learnerId: string, activityId: string) {
    if (!isDemo) return request('/assignments', 'POST', { learnerId: Number(learnerId), activityId: Number(activityId) });
    authorize('Coordinator'); const db = read(); validateAssignment(learnerId, activityId, db);
    db.assignments.push({ id: uid(), learnerId, activityId, status: 'Not Started' }); write(db);
  },
  async updateStatus(id: string, status: Status) {
    if (!STATUSES.includes(status)) fail('Select a valid status.');
    if (!isDemo) {
      const user = getSession();
      const path = user?.role === 'Candidate' ? `/me/assignments/${id}/status` : `/assignments/${id}/status`;
      return request(path, 'PATCH', { status });
    }
    const user = authorize('Candidate'); const db = read();
    const assignment = db.assignments.find(a => a.id === id) || fail('Assignment not found.');
    if (assignment.learnerId !== user.id) fail('You can only update your own training.');
    assignment.status = status; write(db);
  },
  async changePassword(currentPassword: string, newPassword: string): Promise<User> {
    if (newPassword.length < 8) fail('Use at least 8 characters for your new password.');
    if (currentPassword === newPassword) fail('Choose a password different from your temporary password.');
    if (!isDemo) {
      const res = await request<ApiLoginResponse>('/auth/change-password', 'POST', { currentPassword, newPassword });
      return saveSession({ id: String(res.id), name: res.name, email: res.email, role: res.role, mustChangePassword: res.mustChangePassword, version: res.version, expiresAt: res.expiresAt });
    }
    const user = authorize('Candidate', true); const db = read(); const account = db.accounts.find(a => a.learnerId === user.id)!;
    if (account.expiresAt < Date.now() && account.mustChangePassword) fail('Temporary credentials have expired. Ask your coordinator for a reset.');
    if (await hashPassword(currentPassword, account.salt) !== account.hash) fail('The current password is incorrect.');
    account.salt = uid(); account.hash = await hashPassword(newPassword, account.salt); account.mustChangePassword = false; account.version++; write(db);
    return saveSession({ ...user, mustChangePassword: false, version: account.version });
  },
};
