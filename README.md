# Amazon S3 Snapshot Store Plugin for Akka Persistence

An [Akka Persistence](http://doc.akka.io/docs/akka/2.4.1/scala/persistence.html) snapshot store backed by
[Amazon S3](https://aws.amazon.com/s3/)

**Please note that this module only provides Snapshot plugin.**


# Installation

Not published yet.


# Configuration

```
akka.persistence.snapshot-store.plugin = "s3-snapshot-store"

s3-client{
  aws-access-key-id = <your key>
  aws-secret-access-key = <your secret>
  region = "us-west-2"
  endpoint = "default"
  options {
    path-style-access = false
    chunked-encoding-disabled = false
  }
}

s3-snapshot-store {
  bucket-name = "snapshot"
  extension = "ss"
  max-load-attempts = 3
}
```

# S3 key structure

A snapshot object is saved with following key format.

```
<persistenceId>/<reverse sequenceNr>-<timestamp>.<extension>
```

Note that sequenceNr is reversed to optimize performance for partitioning. See http://docs.aws.amazon.com/AmazonS3/latest/dev/request-rate-perf-considerations.html