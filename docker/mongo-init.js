// Runs only when the named MongoDB data volume is initialized for the first time.
const database = db.getSiblingDB('ecocity_esg');
database.createUser({
  user: process.env.MONGO_APP_USER,
  pwd: process.env.MONGO_APP_PASSWORD,
  roles: [{ role: 'readWrite', db: 'ecocity_esg' }]
});
