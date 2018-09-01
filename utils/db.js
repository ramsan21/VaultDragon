"use strict";

var mysql = require('mysql');
var config = require('config');
var pool = mysql.createPool({
    connectionLimit: config.DB_CONNECTION_LIMIT,
    host: config.DB_HOST,
    port: config.DB_PORT,
    user: config.DB_USER,
    password: config.DB_PASSWORD,
    database: config.DB_SCHEMA
});
var debug = require('debug')('key_val_store_api:app:db');
var error = require('debug')('key_val_store_api:app:db:error');


let queries = {
    insert_key: "insert into Repository(skey, svalue, dtime) values ('#skey','#svalue', from_unixtime(#dtime))",
    get_key_by_time: "select skey as 'key',svalue as 'value',unix_timestamp(dtime) as 'timestamp' from Repository where skey='#skey' and dtime <= from_unixtime(#dtime) order by dtime desc limit 1;"
}

let errorResponse = {
    code: 500,
    errorMessage: "DB error"
}

let createKey = (key) => {
    let insertQuery = queries.insert_key.replace('#skey', key.skey)
        .replace('#svalue', key.svalue)
        .replace('#dtime', key.dtime);
    debug("executing :", insertQuery);
    return new Promise((resolve, reject) => {
        pool.query(insertQuery, (err, results, fields) => {
            if (err) {
                error("Error in inserting: %o", err)
                return reject(err);
            }
            debug("Inserted key: %o", results);
            return resolve(key);
        })
    })
}

let getKey = (key) => {
    let getLatestValueQuery = queries.get_key_by_time.replace('#skey', key.skey)
        .replace('#dtime', key.dtime);
    debug("executing :", getLatestValueQuery);
    return new Promise((resolve, reject) => {
        pool.query(getLatestValueQuery, (err, results, fields) => {
            if (err) {
                error("Error in getting latest value: %o", err)
                return reject(err);
            }
            debug("Obtained key: %o", results[0] ? results[0] : {});
            return resolve(results[0] ? results[0] : {});
        })
    })
}

module.exports = {
    createKey: createKey,
    getKey: getKey
}