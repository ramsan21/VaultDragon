var express = require('express');
var router = express.Router();
var debug = require('debug')('key_val_store_api:app:route');
var db = require('../utils/db');


/* GET users listing. */
router.post('/', function (req, res, next) {
    let currentTime = Math.round(new Date().valueOf() / 1000);
    for (var k in req.body) {
        db.createKey({
            skey: k,
            svalue: req.body[k],
            dtime: currentTime
        }).then((success) => {
            res.json(success)
        }).catch((err) => {
            res.status(500);
            res.send(err);
        })
    }

});

router.get('/:key', function (req, res, next) {
    let queryTime = req.query.timestamp ? req.query.timestamp : Math.round(new Date().valueOf() / 1000);
        db.getKey({
            skey: req.params.key,
            dtime: queryTime
        }).then((success) => {
            res.json(success)
        }).catch((err) => {
            res.status(500);
            res.send(err);
        })
});

module.exports = router;