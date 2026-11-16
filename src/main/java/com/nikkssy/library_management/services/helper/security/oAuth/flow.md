Google OIDC authentication
|
v
Validate sub, email, name, email_verified
|
v
Find GOOGLE provider using sub
|
+----+----+
|         |
Found     Not found
|         |
v         v
Existing   Find user by email
user          |
+----+----+
|         |
Found     Not found
|         |
v         v
Existing   Create new user
user          |
|           |
+-----+-----+
|
v
Check account enabled
|
v
Create GOOGLE mapping
|
v
Return OidcUser