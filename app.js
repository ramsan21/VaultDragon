var express = require('express');
require('dotenv').config()
var path = require('path');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var mysql = require('mysql');
var debug = require('debug')('key_val_store_api:app');
var config = require('config');
var connection = mysql.createConnection({
    host: config.DB_HOST,
    port: config.DB_PORT,
    user: config.DB_USER,
    password: config.DB_PASSWORD,
    database: config.DB_SCHEMA
});
  var app = express();
 
  connection.connect(function(err){
  if(!err) {
    console.log("Database is connected ...");  
  } else {
    console.log("Error connecting database ...");  
  }
  connection.end();
  });

var object = require('./routes/object');

var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser())

app.use('/api/v1/object', object);

module.exports = app;
