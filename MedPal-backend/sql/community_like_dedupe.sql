USE medpal;

DELETE t1 FROM community_like t1
INNER JOIN community_like t2
  ON t1.user_id = t2.user_id
 AND t1.target_type = t2.target_type
 AND t1.target_id = t2.target_id
 AND t1.id > t2.id;
