import type { Database } from './types';
export const createSeed = (): Database => ({
  learners: [
    { id: 'jordan', name: 'Jordan Lee', email: 'jordan.lee@example.com', cohort: 'Frontend cohort' },
    { id: 'avery', name: 'Avery Morgan', email: 'avery.morgan@example.com', cohort: 'Frontend cohort' },
    { id: 'riley', name: 'Riley Chen', email: 'riley.chen@example.com', cohort: 'Frontend cohort' },
  ],
  activities: [
    { id: 'react', title: 'React Basics', topic: 'Frontend', plannedHours: 6 },
    { id: 'api', title: 'REST APIs', topic: 'Backend', plannedHours: 5 },
    { id: 'sql', title: 'SQL Foundations', topic: 'Database', plannedHours: 4 },
    { id: 'git', title: 'Git & Collaboration', topic: 'Developer tools', plannedHours: 3 },
  ],
  assignments: [
    { id: 'a1', learnerId: 'jordan', activityId: 'react', status: 'In Progress' },
    { id: 'a2', learnerId: 'jordan', activityId: 'api', status: 'Not Started' },
    { id: 'a3', learnerId: 'jordan', activityId: 'sql', status: 'Completed' },
    { id: 'a4', learnerId: 'avery', activityId: 'react', status: 'Completed' },
    { id: 'a5', learnerId: 'avery', activityId: 'api', status: 'Completed' },
    { id: 'a6', learnerId: 'avery', activityId: 'git', status: 'In Progress' },
    { id: 'a7', learnerId: 'riley', activityId: 'sql', status: 'Completed' },
    { id: 'a8', learnerId: 'riley', activityId: 'git', status: 'Completed' },
  ],
  accounts: [],
});
