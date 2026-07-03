-- SpeakOut seed data — CSC584 Group 3
-- Schools: first 25 Selangor rows of the 2022 MOE senarai sekolah menengah CSV.
-- Demo logins: admin@speakout.demo/Admin@123, teacher.aina@speakout.demo/Teacher@123, student.aiman@speakout.demo/Student@123
-- Re-runnable: clears all tables first.

TRUNCATE evidence, case_notes, cases, reports, category, users, school CASCADE;

-- ── Schools (MOE Selangor subset) ────────────────────────────────────────────
INSERT INTO school (school_id, code, name, address) VALUES
('SCH-001', 'BEA0091', 'Sekolah Menengah Kebangsaan Raja Mahadi', 'Taman Klang Jaya, 41200 Klang'),
('SCH-002', 'BEA0092', 'Sekolah Menengah Kebangsaan Raja Lumu', 'Jalan Raja Lumu, Pandamaran, 42000 Pelabuhan Klang'),
('SCH-003', 'BEA0093', 'Sekolah Menengah Kebangsaan Tengku Ampuan Rahimah', 'Persiaran Raja Muda Musa, 41200 Klang'),
('SCH-004', 'BEA0094', 'Sekolah Menengah Kebangsaan Tengku Ampuan Jemaah', 'Jalan Pandamaran, 42000 Pelabuhan Klang'),
('SCH-005', 'BEA0095', 'Sekolah Menengah Kebangsaan Tengku Idris Shah', 'Jalan Tap, 42200 Kapar'),
('SCH-006', 'BEA0096', 'Sekolah Menengah Kebangsaan Sultan Abdul Samad', 'Jalan Landasan, Bukit Kuda, 41300 Klang'),
('SCH-007', 'BEA0097', 'Sekolah Menengah Kebangsaan Pulau Indah', 'Lot 79478, Persiaran Pulau Lumut Km12, Seksyen 10, 42920 Pulau Indah'),
('SCH-008', 'BEA0099', 'Kolej Tingkatan Enam Sri Istana', 'Jalan Istana, 41000 Klang'),
('SCH-009', 'BEA0100', 'Sekolah Menengah Kebangsaan Rantau Panjang', 'Jalan Kapar, 42100 Klang'),
('SCH-010', 'BEA0101', 'Sekolah Menengah Kebangsaan (P) Kapar', 'Persiaran Hamzah Alang, 42200 Kapar'),
('SCH-011', 'BEA0102', 'Sekolah Menengah Kebangsaan Pendamaran Jaya', 'Jalan Young, Pandamaran, 42000 Pelabuhan Klang'),
('SCH-012', 'BEA0103', 'Sekolah Menengah Kebangsaan Telok Gadong', 'Jalan Serampang 3, Off Jalan Teluk Pulai, 41100 Klang'),
('SCH-013', 'BEA0104', 'Sekolah Menengah Kebangsaan Sri Andalas', 'Jalan Sri Damak 10, Taman Sri Andalas, 41200 Klang'),
('SCH-014', 'BEA0105', 'Sekolah Menengah Kebangsaan Jalan Kebun', 'Batu 7 Jalan Kebun Seksyen 32, 40460 Shah Alam'),
('SCH-015', 'BEA0106', 'Sekolah Menengah Kebangsaan Kampung Jawa', 'Jalan Raja Nong, 41000 Klang'),
('SCH-016', 'BEA0107', 'Sekolah Menengah Kebangsaan Taman Klang Utama', 'Persiaran Sg. Keramat, Taman Klang Utama, 42100 Klang'),
('SCH-017', 'BEA0108', 'Sekolah Menengah Kebangsaan Kota Kemuning', 'Lot 4, Jln Anggerik Doritis 31/143, Kota Kemuning, 40460 Shah Alam'),
('SCH-018', 'BEA0109', 'Sekolah Menengah Kebangsaan Bandar Baru Sultan Suleiman', 'Lebuh Sultan Abdul Samad, 42000 Pelabuhan Klang'),
('SCH-019', 'BEA0110', 'Sekolah Menengah Kebangsaan Sungai Kapar Indah', 'Jalan Harapan 3A, Taman Sungai Kapar Indah, 42200 Kapar, Klang'),
('SCH-020', 'BEA0111', 'Sekolah Menengah Kebangsaan Shahbandaraya', 'Jalan Raja Nong, 41000 Klang'),
('SCH-021', 'BEA0112', 'Sekolah Menengah Kebangsaan Batu Unjur', 'Jalan Nilam 1, Bandar Bukit Tinggi, 41200 Klang'),
('SCH-022', 'BEA0113', 'Sekolah Menengah Kebangsaan Bukit Tinggi Klang', 'Jalan Batu Nilam 1, 41200 Klang'),
('SCH-023', 'BEA0114', 'Sekolah Menengah Kebangsaan Bukit Kapar', 'Jalan Abdul Wahid, Bukit Kapar, 42200 Kapar'),
('SCH-024', 'BEA1064', 'Sekolah Menengah Kebangsaan Jenjarom', 'Kg Jenjarom, 42600 Jenjarom'),
('SCH-025', 'BEA1065', 'Sekolah Menengah Kebangsaan Jugra', 'Jalan Sultan Abdul Samad, 42700 Banting');

-- ── Categories ──────────────────────────────────────────────────────────────
INSERT INTO category (category_id, name, description) VALUES
('CAT-VERBAL',   'Verbal',   'Name-calling, insults, threats, mocking'),
('CAT-CYBER',    'Cyber',    'Harassment via social media, messaging apps, or online groups'),
('CAT-PHYSICAL', 'Physical', 'Hitting, pushing, property damage, physical intimidation'),
('CAT-SOCIAL',   'Social',   'Exclusion, rumour-spreading, social isolation');

-- ── Demo accounts ────────────────────────────────────────────────────────────
-- admin@speakout.demo / Admin@123 · teacher.*@speakout.demo / Teacher@123 · student.*@speakout.demo / Student@123
INSERT INTO users (user_id, school_id, full_name, email, password_hash, role, class_form) VALUES
('USR-ADMIN', 'SCH-001', 'Pn. Salmah binti Osman',    'admin@speakout.demo',         '$2a$10$j3XC/5UFEjEDplu3l0XLZuj9QOPIPoPqlv1e55r5FxB6lxl0Xp9nO', 'Admin',   NULL),
('USR-T001',  'SCH-001', 'Pn. Aina binti Rahman',     'teacher.aina@speakout.demo',  '$2a$10$h.e9d0sIX8.QCqeEGQG3AuSPlWhDLgB6zpnKRN5z2YkkJ9NLZIdxK', 'Teacher', NULL),
('USR-T002',  'SCH-002', 'En. Farid bin Kamaruddin',  'teacher.farid@speakout.demo', '$2a$10$h.e9d0sIX8.QCqeEGQG3AuSPlWhDLgB6zpnKRN5z2YkkJ9NLZIdxK', 'Teacher', NULL),
('USR-S001',  'SCH-001', 'Aiman bin Zulkifli',        'student.aiman@speakout.demo', '$2a$10$FH9PDBrA.YjdlR2qyxqu8ecnwa/ISsJwV4pYsYEE/OB2B.So/sCGe', 'Student', 'Form 4 Bestari'),
('USR-S002',  'SCH-001', 'Tan Mei Ling',              'student.mei@speakout.demo',   '$2a$10$FH9PDBrA.YjdlR2qyxqu8ecnwa/ISsJwV4pYsYEE/OB2B.So/sCGe', 'Student', 'Form 2 Cekal'),
('USR-S003',  'SCH-002', 'Harith bin Iskandar',       'student.harith@speakout.demo','$2a$10$FH9PDBrA.YjdlR2qyxqu8ecnwa/ISsJwV4pYsYEE/OB2B.So/sCGe', 'Student', 'Form 5 Amanah'),
('USR-S004',  'SCH-003', 'Nurul Izzah binti Hamzah',  'student.nurul@speakout.demo', '$2a$10$FH9PDBrA.YjdlR2qyxqu8ecnwa/ISsJwV4pYsYEE/OB2B.So/sCGe', 'Student', 'Form 3 Dinamik');

-- ── Sample reports (Feb–Jun 2026, mixed status/category/school for charts) ──
INSERT INTO reports (report_id, school_id, reporter_id, category_id, title, description, location, incident_date, severity, anonymity_flag, status, submitted_at, updated_at) VALUES
('SO-0401', 'SCH-001', 'USR-S001', 'CAT-VERBAL',   'Repeated name-calling in class',        'A group of classmates keeps calling me names during recess and in class when the teacher is not looking.', 'Classroom 4B',    '2026-02-08', 'Medium',   TRUE,  'Resolved',  '2026-02-09 08:30:00', '2026-03-02 15:00:00'),
('SO-0402', 'SCH-001', 'USR-S002', 'CAT-CYBER',    'Mean messages in class group chat',     'Screenshots of me are being shared in the class WhatsApp group with insulting captions.',                  'Online',          '2026-02-20', 'High',     TRUE,  'Resolved',  '2026-02-21 10:15:00', '2026-03-18 09:40:00'),
('SO-0403', 'SCH-002', 'USR-S003', 'CAT-PHYSICAL', 'Pushed near the canteen',               'Two seniors pushed me against the wall near the canteen and took my food money.',                          'Canteen',         '2026-03-04', 'High',     TRUE,  'In review', '2026-03-04 13:05:00', '2026-03-06 11:20:00'),
('SO-0404', 'SCH-001', 'USR-S001', 'CAT-SOCIAL',   'Excluded from group activities',        'My usual group refuses to let me join any group work and tells others to avoid me.',                       'Classroom 4B',    '2026-03-15', 'Low',      TRUE,  'Resolved',  '2026-03-16 09:00:00', '2026-04-10 14:30:00'),
('SO-0405', 'SCH-003', 'USR-S004', 'CAT-VERBAL',   'Threatened after sports practice',      'A student from another class threatened to hurt me if I report what I saw during practice.',               'Sports field',    '2026-04-02', 'Critical', TRUE,  'In review', '2026-04-02 17:45:00', '2026-04-03 08:10:00'),
('SO-0406', 'SCH-001', 'USR-S002', 'CAT-CYBER',    'Fake account posting about me',         'Someone made a fake Instagram account using my photos and is posting embarrassing things.',                'Online',          '2026-04-18', 'High',     TRUE,  'In review', '2026-04-19 11:30:00', '2026-04-22 16:00:00'),
('SO-0407', 'SCH-002', 'USR-S003', 'CAT-SOCIAL',   'Rumours spread about my family',        'False rumours about my family are being spread and now most of my class avoids me.',                       'Classroom 5A',    '2026-05-06', 'Medium',   TRUE,  'In review', '2026-05-07 08:50:00', '2026-05-09 10:05:00'),
('SO-0408', 'SCH-001', 'USR-S001', 'CAT-PHYSICAL', 'Tripped on the stairs on purpose',      'The same senior keeps blocking and tripping me on the staircase between classes.',                         'Stairwell Block B','2026-05-21', 'Medium',  TRUE,  'In review', '2026-05-21 14:20:00', '2026-05-23 09:15:00'),
('SO-0409', 'SCH-003', 'USR-S004', 'CAT-VERBAL',   'Mocked during oral presentation',       'Students laughed and shouted insults during my presentation and the mocking continued after class.',       'Classroom 3D',    '2026-06-10', 'Low',      TRUE,  'Submitted', '2026-06-11 09:25:00', '2026-06-11 09:25:00'),
('SO-0410', 'SCH-001', 'USR-S002', 'CAT-SOCIAL',   'Left out of class events',              'I am deliberately not told about class events and gatherings organised by the class committee.',           'Classroom 2C',    '2026-06-25', 'Low',      TRUE,  'Submitted', '2026-06-28 15:40:00', '2026-06-28 15:40:00'),
('SO-0411', 'SCH-001', 'USR-S001', 'CAT-CYBER',    'Group chat harassment (draft)',         'Still collecting screenshots before I submit this.',                                                       'Online',          '2026-06-29', 'Medium',   TRUE,  'Draft',     '2026-06-30 20:10:00', '2026-06-30 20:10:00'),
('SO-0412', 'SCH-002', 'USR-S003', 'CAT-PHYSICAL', 'Locker damaged again (draft)',          'My locker was forced open and my books were thrown out. Second time this month.',                          'Locker area',     '2026-07-01', 'Medium',   TRUE,  'Draft',     '2026-07-01 12:00:00', '2026-07-01 12:00:00');

-- ── Cases for reports under handling ─────────────────────────────────────────
INSERT INTO cases (case_id, report_id, assigned_to, status, priority, created_at, updated_at) VALUES
('CS-0401', 'SO-0401', 'USR-T001', 'Closed',              'Medium',   '2026-02-10 09:00:00', '2026-03-02 15:00:00'),
('CS-0402', 'SO-0402', 'USR-T001', 'Resolved',            'High',     '2026-02-22 08:30:00', '2026-03-18 09:40:00'),
('CS-0403', 'SO-0403', 'USR-T002', 'Under Investigation', 'High',     '2026-03-05 09:10:00', '2026-03-06 11:20:00'),
('CS-0404', 'SO-0404', 'USR-T001', 'Resolved',            'Low',      '2026-03-17 10:00:00', '2026-04-10 14:30:00'),
('CS-0405', 'SO-0405', 'USR-T002', 'Under Investigation', 'Critical', '2026-04-03 08:00:00', '2026-04-03 08:10:00'),
('CS-0406', 'SO-0406', 'USR-T001', 'Under Investigation', 'High',     '2026-04-20 09:30:00', '2026-04-22 16:00:00'),
('CS-0407', 'SO-0407', 'USR-T002', 'Under Investigation', 'Medium',   '2026-05-08 08:45:00', '2026-05-09 10:05:00'),
('CS-0408', 'SO-0408', 'USR-T001', 'New',                 'Medium',   '2026-05-22 11:00:00', '2026-05-23 09:15:00');

-- ── Investigation notes (timeline entries) ───────────────────────────────────
INSERT INTO case_notes (note_id, case_id, author_id, body, visibility, created_at) VALUES
('NT-0001', 'CS-0401', 'USR-T001', 'Spoke with the reporter to confirm details. Identified the students involved.',                'internal',         '2026-02-12 10:30:00'),
('NT-0002', 'CS-0401', 'USR-T001', 'Met both parties with parents present. Mediation session completed, behaviour contract signed.','reporter-visible', '2026-02-25 14:00:00'),
('NT-0003', 'CS-0401', 'USR-T001', 'Follow-up check after one week — no recurrence. Closing the case.',                             'reporter-visible', '2026-03-02 15:00:00'),
('NT-0004', 'CS-0402', 'USR-T001', 'Collected screenshots as evidence. Referred to the discipline teacher.',                        'internal',         '2026-02-24 09:20:00'),
('NT-0005', 'CS-0402', 'USR-T001', 'Group chat moderated, students counselled. Reporter informed of the outcome.',                  'reporter-visible', '2026-03-18 09:40:00'),
('NT-0006', 'CS-0403', 'USR-T002', 'Reviewed canteen CCTV footage with the discipline unit.',                                       'internal',         '2026-03-06 11:20:00'),
('NT-0007', 'CS-0405', 'USR-T002', 'Escalated to the principal due to threat severity. Interview scheduled.',                       'internal',         '2026-04-03 08:10:00'),
('NT-0008', 'CS-0406', 'USR-T001', 'Reported the fake account to the platform; awaiting takedown confirmation.',                    'reporter-visible', '2026-04-22 16:00:00');

-- ── Evidence ─────────────────────────────────────────────────────────────────
INSERT INTO evidence (evidence_id, report_id, file_url, file_type, file_size, uploaded_at) VALUES
('EV-0001', 'SO-0402', '/uploads/SO-0402/groupchat-screenshot-1.png', 'image',    482113,  '2026-02-21 10:15:00'),
('EV-0002', 'SO-0402', '/uploads/SO-0402/groupchat-screenshot-2.png', 'image',    391226,  '2026-02-21 10:16:00'),
('EV-0003', 'SO-0406', '/uploads/SO-0406/fake-account-profile.png',   'image',    655012,  '2026-04-19 11:30:00'),
('EV-0004', 'SO-0403', '/uploads/SO-0403/canteen-incident.mp4',       'video',    8123400, '2026-03-04 13:05:00');

-- Restart ID sequences past the seeded rows
ALTER SEQUENCE report_seq   RESTART WITH 413;
ALTER SEQUENCE case_seq     RESTART WITH 409;
ALTER SEQUENCE note_seq     RESTART WITH 9;
ALTER SEQUENCE evidence_seq RESTART WITH 5;
ALTER SEQUENCE user_seq     RESTART WITH 1001;
