# INSERT INTO member_point (member_id, balance, create_date, update_date) VALUES(1234, 100, timestampadd(day, -2, current_date), timestampadd(day, -2, current_date));
# INSERT INTO member_point_history (id, member_id, order_no, action, amount, description, create_date)
# VALUES (1, 1234, "abcd", "earn", 100, "적립", timestampadd(day, -2, current_date));

# INSERT INTO member_point_expire (id, member_id, order_no, expire_day, expire_amount, is_admin, create_date, update_date)
# VALUES (1, 1234, "abcd", "2024-10-01", 100, 0, timestampadd(day, -2, current_date), timestampadd(day, -2, current_date));