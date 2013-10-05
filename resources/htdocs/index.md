# Welcome to Kiries

Kiries is an interactive time-series data collection, analysis and
exploration workbench.  More specifically, it is a
[Riemann](http://riemann.io) server, that can archive and index events
in [ElasticSearch](http://elasticsearch.org), like
[LogStash](http://logstash.net) does.  Then you can use
[Kibana](http://elasticsearch.org/overview/kibana) to:

* Collect data from your infrastructure, logs, processes, and dog.
* Explore your time-series data as it happens, or in the past
* Keep archives of system behavior over days, weeks, months or longer
* Build interactive dashboards with specific views of your data
* Share the dashboards with others, including previously mentioned dog.
* Manage your indexes using [ElasticSearchHQ](http://elastic hq.org)

What does all that really mean?  Check it out, Kiries is already
indexing it's own JVM metrics. You can see that data in the
[Kiries JVM Metrics Dashboard](kibana/index.html#/dashboard/file/jvm-metrics.json).

## Getting Data Into Kiries

There are several options to getting data into Kiries.  The major
considerations are:

 1. Mapping it into Riemann Events
 2. Mapping it into ElasticSearch Event
 3. Defining a document type in ES
 4. Picking an index name for it, and how to break it up over time.
 5. Telling ES how to index the data
 6. Expiring data.
 7. Delivering it to Riemann or ES

Let's start with some simple example of getting data into Kiries

### From Riemann clients

The easiest way to get data into it, is to send Riemann events to it.
You can do this directly from a
[Riemann client](http://riemann.io/clients.html), like
[riemann-tools](https://github.com/aphyr/riemann-tools).

<div style="width:600px">
  <pre>
  # Assuming you have ruby and rubygems installed
  gem install riemann-tools

  # add a tag so we can pick these events out
  # see config/riemann-health.config for an example
  riemann-health --tagged riemann-health --host my.kiries.server 
  </pre>
</div>

And then you can see that in the example
[Riemann Host Health Dashboard](kibana/index.html#/dashboard/file/riemann-health.json)

### From an existing Riemann server

For example you could use this snippet to forward all of your Riemann
events from your current Riemann server to your Kiries instance:

<div style="width:600px">
    <pre>
   (streams
      ; forward all non-riemann events to Kiries
      (where (not (service #"^riemann"))
        (let [client (tcp-client :host "kirieshost" :port 5555)]
          (forward client))))
    </pre>
</div>

Once the data is getting to Kiries, you can then control how it's
indexed in the `config/riemann.confg` file.  If you're not quite sure
what the hell you just indexed, you can use the
[WTF Dashboard](kibana/index.html#/dashboard/file/wtf.json) to get a
rawish view of your indexes events.

### From Logstash

Logstash can index events directly to ElasticSearch.  Assuming we have
logstash set up to parse Apache request logs already, we could point
the elasticseach_http output module at Kiries with this Logstash
config snippet:

<div style="width:600px">
  <pre>
  	elasticsearch_http {
	    index => "http-requests-%{+YYYY.MM.dd}"
        # Kiries provides a default ES mapping for http requests
	    index_type => "http-request"
 	    host => "127.0.0.1"
	    port => 9200
        }
  </pre>	
</div>

## <span id="dashboards">Dashboards</a>

For a quick video introduction to Kibana, check out these clips:

* [Building Dashboards](https://www.youtube.com/watch?feature=player_embedded&v=xjIMbn2ib-0)

* [Using Kibana 3 with LogStash](https://www.youtube.com/watch?v=hXiBe8NcLPA)

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

</div>

