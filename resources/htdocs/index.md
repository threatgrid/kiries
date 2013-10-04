# Welcome to Kiries

## Getting Data Into Kiries

Kiries is a Riemann server, that will dump events it recieved into
ElasticSearch, like LogStash does.  So the easiest way to get data
into it, is to send Riemann events too it.

Kiries is already indexing it's own JVM metrics. You can see that data
in the [Kiries JVM Metrics Dashboard](kibana/index.html#/dashboard/file/jvm-metrics.json).

For example you could use this snippet to forward all of your Riemann
events from your current riemann server to your Kiries instance:

    (streams
      ; forward all non-riemann events to Kiries
      (where (not (service #"^riemann"))
        (let [client (tcp-client :host "kirieshost" :port 5555)]
          (forward client))))

Once the data is getting to Kiries, you can then control how it's
indexed in the `config/riemann.confg` file.

## Dashboards

[Kibana's](kibana/index.html) home page has some example dashboards.

You can also put your own dashboards in the
`resources/kibana/dashboard/` directory and visit them at
`/kibana/index.html#/dashboard/file/FILENAME.json`


## Managing ElasticSearch

We've bundled [ElasticSearch HQ](/HQ/index.html), a great ES cluster
management plugin.  You can control the ElasticSearch instance inside
of Kiries with the `config/elasticsearch.yml` file.

We have optimized for time series data which will be broken up into
per hour|day|week|month|year indexes, and is relatively transient.
So, we use 1 shard per index and no replication.

We also load up a set of document mappings that are optimized for
time-series data.  You can see those in `config/default-mapping.json`
and modify as you see fit.  There are examples on how to load your own
mapping in the `config/riemann.config` file as well.



