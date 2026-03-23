-- /Users/yamingdeng/Projects/billing-ws/billing/src/main/resources/db/migration/V14__remove_detail_type_from_feature_access_logs.sql
ALTER TABLE feature_access_logs DROP COLUMN detail_type;
