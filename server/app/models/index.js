"use strict";

var fs        = require("fs");
var path      = require("path");

var env       = process.env.NODE_ENV || "development";
var db = require('../../config/sequelize');
//var config    = require(path.join(__dirname, '..', 'config', 'config.json'))[env];
var sequelize = db.sequelize;
var Sequelize = db.Sequelize;


var dbmodel = {};

fs
  .readdirSync(__dirname)
  .filter(function(file) {
    return (file.indexOf(".") !== 0) && (file !== "index.js");
  })
  .forEach(function(file) {
    var model = sequelize.import(path.join(__dirname, file));
    dbmodel[model.name] = model;
  });

Object.keys(dbmodel).forEach(function(modelName) {
  if ("associate" in dbmodel[modelName]) {
    dbmodel[modelName].associate(dbmodel);
  }
});

dbmodel.sequelize = db.sequelize;
dbmodel.Sequelize = db.Sequelize;

dbmodel.clubs.hasMany(dbmodel.events,{foreignKey:'associated_events', constraints: true});
dbmodel.events.belongsTo(dbmodel.clubs,{foreignKey:'associated_club', constraints: true });

// dbmodel.clubs.belongsToMany(dbmodel.users,{through:'clubs_users',foreignKey:'subscribers', constraints: true});
// dbmodel.users.belongsToMany(dbmodel.clubs,{through:'clubs_users',foreignKey:'subscribed_clubs', constraints: true});

dbmodel.clubs.belongsToMany(dbmodel.users,{as:'Subscribers', through: 'clubs_users', foreignKey:'subscribed_clubs', constraints: true});
dbmodel.users.belongsToMany(dbmodel.clubs,{as:'SubscribedClubs', through: 'clubs_users', foreignKey:'subscribers', constraints: true});

dbmodel.events.belongsToMany(dbmodel.users,{as:'Subscribers', through: 'events_users', foreignKey:'subscribed_events', constraints: true});
dbmodel.users.belongsToMany(dbmodel.events,{as:'SubscribedEvents', through: 'events_users', foreignKey:'subscribers', constraints: true});

// dbmodel.clubs.belongsToMany(dbmodel.users,{through:'clubs_users',foreignKey:'subscribers'});
// dbmodel.users.belongsToMany(dbmodel.clubs,{through:'clubs_users',foreignKey:'subscribed_clubs'});

// dbmodel.events.belongsToMany(dbmodel.users,{through: 'events_users', foreignKey:'subscribers'});
// dbmodel.users.belongsToMany(dbmodel.events,{through: 'events_users',foreignKey:'subscribed_events'});

dbmodel.clubs.hasMany(dbmodel.users,{foreignKey:'admins', constraints: true});

dbmodel.events.hasMany(dbmodel.users,{foreignKey:'organizers', constraints: true});


module.exports = dbmodel;