## command for create database

## in Mongo
1. Create Character Database
```
mongoimport -h localhost:27017 -d monopoly -c characters --type json --jsonArray characters.json
mongoimport -h localhost:27017 -d monopoly -c properties --type json properties.json
```

2. Create Property Database
