-- Run once on an existing database after reviewing and resolving duplicate usernames.
-- Check duplicates first:
-- SELECT username, COUNT(*) FROM `user` GROUP BY username HAVING COUNT(*) > 1;
ALTER TABLE `user`
    ADD CONSTRAINT `uk_user_username` UNIQUE (`username`);
