# Maintain trust assets frontend

This service is responsible for updating the information held about assets in a trust registration.

The service allows a user to update their Non-EEA companies in a standard maintenance joureny.

The service allows a user to update the following on a non-taxable to taxable migration journey:
- Money
- Shares
- Property or land
- Business
- Non-eea company
- Other

To run locally using the micro-service provided by the service manager:

```
sm2 --start TRUSTS_ALL
```

If you want to run your local copy, then stop the frontend ran by the service manager and run your local code by using the following (port number is 9800 but is defaulted to that in build.sbt).

```
sbt run
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
