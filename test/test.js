process.env.NODE_ENV = 'test';

//Require the dev-dependencies
let chai = require('chai');
let chaiHttp = require('chai-http');
let expect = require('expect');
let app = require('../app');
let should = chai.should();


chai.use(chaiHttp);
// //Our parent block
describe('Testing Key Val Store', () => {
    // beforeEach((done) => { //Before each test we empty the database
    //     Book.remove({}, (err) => { 
    //        done();           
    //     });        
    // });
/*
  * Test the /POST route
  */
  describe('/POST create a Key', () => {
      it('it should POST and create a new key', (done) => {
        chai.request(app)
            .post('/api/v1/object')
            .send({
                "test1":"testvalue1"
            })
            .end((err, res) => {
                  res.should.have.status(200);
                  res.body.should.be.a('object');
                  let resBody = res.body;
                  expect(resBody.skey).toBe("test1")
                  expect(resBody.svalue).toBe("testvalue1")
              done();
            });
      });
  });

  describe('/GET a Key', () => {
    it('it should GET a key previously created', (done) => {
      chai.request(app)
          .get('/api/v1/object/demo1')
          .end((err, res) => {
                res.should.have.status(200);
                res.body.should.be.a('object');
                let resBody = res.body;
                expect(resBody.key).toBe("test1")
                expect(resBody.value).toBe("testvalue1")
            done();
          });
    });
});

describe('/GET a Key with timestamp', () => {
    it('it should GET a key previously created when timestamp is passed', (done) => {
        let currentTime = Math.round(new Date().valueOf() / 1000);
      chai.request(app)
          .get('/api/v1/object/test1?timestamp='+currentTime)
          .end((err, res) => {
                res.should.have.status(200);
                res.body.should.be.a('object');
                let resBody = res.body;
                expect(resBody.key).toBe("test1")
                expect(resBody.value).toBe("testvalue1")
            done();
          });
    });
});

});