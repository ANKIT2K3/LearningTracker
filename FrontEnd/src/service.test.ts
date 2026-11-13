// @vitest-environment jsdom
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { webcrypto } from 'node:crypto';
import { service, getSession, summary, validateActivity, validateAssignment, validateLearner } from './service';
import { createSeed } from './seed';

Object.defineProperty(globalThis, 'crypto', { value: webcrypto, configurable: true });
beforeEach(() => { localStorage.clear(); sessionStorage.clear(); vi.restoreAllMocks(); });

describe('capstone data rules', () => {
  it('requires a title, topic and positive finite hours', () => {
    for (const input of [ { title: ' ', topic: 'Frontend', plannedHours: 3 }, { title: 'React', topic: '', plannedHours: 3 }, { title: 'React', topic: 'Frontend', plannedHours: 0 }, { title: 'React', topic: 'Frontend', plannedHours: NaN } ]) expect(() => validateActivity(input)).toThrow();
    expect(() => validateActivity({ title: 'React', topic: 'Frontend', plannedHours: .5 })).not.toThrow();
  });
  it('validates candidate identity and case-insensitive email uniqueness', () => {
    expect(() => validateLearner({ name: 'New', email: 'JORDAN.LEE@example.com', cohort: 'Cohort' }, createSeed().learners)).toThrow(/already exists/);
    expect(() => validateLearner({ name: '', email: 'bad', cohort: '' }, [])).toThrow();
    expect(() => validateLearner({ name: 'New', email: 'bad', cohort: 'Cohort' }, [])).toThrow(/valid email/);
  });
  it('rejects duplicate assignments and invalid references', () => {
    expect(() => validateAssignment('jordan', 'react', createSeed())).toThrow(/already assigned/);
    expect(() => validateAssignment('unknown', 'react', createSeed())).toThrow(/existing/);
  });
  it('calculates summary from saved assignments and handles empty data', () => {
    expect(summary(createSeed().assignments)).toEqual({ total: 8, completed: 5, inProgress: 2, notStarted: 1, percent: 63 });
    expect(summary([]).percent).toBe(0);
  });
});

describe('frontend workflow and role boundaries', () => {
  it('creates a candidate, assigns an activity, enforces first-login setup, saves progress and restores the summary', async () => {
    await service.demo('Coordinator');
    const activity = await service.saveActivity({ title: 'TypeScript', topic: 'Frontend', plannedHours: 2.5 });
    await service.saveActivity({ title: 'TypeScript Foundations', topic: 'Frontend', plannedHours: 3 }, activity.id);
    const credentials = await service.addLearner({ name: 'Taylor Brooks', email: 'Taylor@example.com', cohort: 'Frontend cohort' });
    const candidate = (await service.workspace()).learners.find(l => l.email === 'taylor@example.com')!;
    await service.assign(candidate.id, activity.id);
    await expect(service.assign(candidate.id, activity.id)).rejects.toThrow(/already assigned/);
    await service.logout();
    const user = await service.login(credentials.email, credentials.temporaryPassword);
    expect(user.mustChangePassword).toBe(true);
    await expect(service.workspace()).rejects.toThrow(/Set your new password/);
    await expect(service.changePassword(credentials.temporaryPassword, 'short')).rejects.toThrow(/8 characters/);
    await service.changePassword(credentials.temporaryPassword, 'A-test-only-password!');
    const own = await service.workspace();
    expect(own.learners.map(l => l.id)).toEqual([candidate.id]);
    expect(own.activities[0].title).toBe('TypeScript Foundations');
    await service.updateStatus(own.assignments[0].id, 'Completed');
    expect(summary((await service.workspace()).assignments).completed).toBe(1);
    await service.logout();
    await expect(service.workspace()).rejects.toThrow(/expired/);
    await service.login(credentials.email, 'A-test-only-password!');
    expect((await service.workspace()).assignments[0].status).toBe('Completed');
    await service.logout();
    await service.demo('Coordinator');
    expect(summary((await service.workspace()).assignments).completed).toBe(6);
    const storage = localStorage.getItem('learnflow.workspace.v1')!;
    expect(storage).not.toContain(credentials.temporaryPassword);
    expect(storage).not.toContain('A-test-only-password!');
  });
  it('rejects candidate coordinator actions and access to another learner’s assignment', async () => {
    await service.demo('Candidate');
    await expect(service.addLearner({ name: 'New', email: 'new@example.com', cohort: 'C' })).rejects.toThrow(/permission/);
    await expect(service.saveActivity({ title: 'X', topic: 'Y', plannedHours: 2 })).rejects.toThrow(/permission/);
    await expect(service.assign('jordan', 'git')).rejects.toThrow(/permission/);
    await expect(service.updateStatus('a4', 'Completed')).rejects.toThrow(/own training/);
    await expect(service.resetPassword('avery')).rejects.toThrow(/permission/);
  });
  it('rejects invalid credentials and expires temporary passwords after 24 hours', async () => {
    await service.demo('Coordinator');
    const credentials = await service.addLearner({ name: 'Test Person', email: 'person@example.com', cohort: 'C' });
    await service.logout();
    await expect(service.login(credentials.email, 'incorrect')).rejects.toThrow(/Unable to sign in/);
    const future = Date.now() + 86400001;
    vi.spyOn(Date, 'now').mockReturnValue(future);
    await expect(service.login(credentials.email, credentials.temporaryPassword)).rejects.toThrow(/Unable to sign in/);
  });
  it('invalidates previous sessions and passwords after a coordinator reset', async () => {
    await service.demo('Candidate');
    const oldSession = getSession()!;
    await service.demo('Coordinator');
    const credentials = await service.resetPassword('jordan');
    sessionStorage.setItem('learnflow.session.v1', JSON.stringify(oldSession));
    await expect(service.workspace()).rejects.toThrow(/session has ended/);
    expect(getSession()).toBeNull();
    expect((await service.login(credentials.email, credentials.temporaryPassword)).mustChangePassword).toBe(true);
  });
  it('rejects expired sessions and preserves existing data when storage writes fail', async () => {
    await service.demo('Coordinator');
    const session = getSession()!;
    vi.spyOn(Date, 'now').mockReturnValue(session.expiresAt + 1);
    await expect(service.workspace()).rejects.toThrow(/expired/);
    vi.restoreAllMocks();
    await service.demo('Coordinator');
    const original = localStorage.getItem('learnflow.workspace.v1');
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('Quota exceeded'); });
    await expect(service.saveActivity({ title: 'X', topic: 'Y', plannedHours: 1 })).rejects.toThrow(/could not be saved/);
    expect(localStorage.getItem('learnflow.workspace.v1')).toBe(original);
  });
});
