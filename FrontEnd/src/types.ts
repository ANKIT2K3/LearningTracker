export type Status = 'Not Started' | 'In Progress' | 'Completed';
export type Role = 'Coordinator' | 'Candidate';
export interface Learner { id: string; name: string; email: string; cohort: string }
export interface Activity { id: string; title: string; topic: string; plannedHours: number }
export interface Assignment { id: string; learnerId: string; activityId: string; status: Status }
export interface User { id: string; name: string; email: string; role: Role; mustChangePassword: boolean; version: number; expiresAt: number }
export interface Workspace { learners: Learner[]; activities: Activity[]; assignments: Assignment[] }
export interface Credentials { email: string; temporaryPassword: string; expiresAt: number }
export interface Account { learnerId: string; hash: string; salt: string; mustChangePassword: boolean; expiresAt: number; version: number }
export interface Database extends Workspace { accounts: Account[] }
