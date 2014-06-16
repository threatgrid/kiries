# Kiries - KIbana, RIemann and ElasticSearch

We wanted a point-and-shoot real-time trend analysis dashboard for our
Riemann-based cluster monitoring, as well as for other generic
time-series data sources.  So, we glued our favorite tools together
with Clojure.

We include the following versions of the third-party components

* [Kibana](http://www.elasticsearch.org/overview/kibana/) (3.1.0)
* [Riemann](http://riemann.io) (0.2.5)
* [Elastic Search](http://www.elasticsearch.org) (1.1.1)
* [Elastic HQ](http://elastichq.org) (ea630c8)

Those projects have all done incredible work, and Kiries is just a
little glue and some documentation tying them together.

Any clojure dependencies are described in the `project.clj`.

Check out the [User Guide](resources/htdocs/index.md) for more details.

# Quickstart

__WARNING__ : Kiries will open publicly accessible ports on your host.
Read the installation instructions below, or live dangerously

Unpack an archive or checkout the repo and run:

    bin/kiries

And point your browser at <http://localhost:9090/index.html>

# Installation

Unpack the archive, or open up the git repository, and you will see a
directory structure like this:

 * `config` -- configuration files for riemann and ElasticSearch
 * `resources/htdocs` -- Web documents, including Kibana, and it configuration in `kibana/config.js`
 * `src` -- Kiries src files
 * `logs` -- log files
 * `bin` -- helper shell scripts
 * `lib` -- libraries and jars

__WARNING__ : Kiries will open several publically accesible ports on your host.

 * 9090 -- Webserver, serving up Kibana and docs
 * 9200,9300 -- ElasticSearch HTTP and Native APIs
 * 5555 -- Riemann listeners (tcp and udp)

Read the following and take precautions as you see fit.

By default, Kiries will start up Riemann tcp and udp servers on port
`5555`.  It will index all events it receives into ElasticSearch.  To
customize this behavior, edit the `config/riemann.config` file.

Kiries will also start up ElasticSearch listening on the default
ports.  ES will store it's data in the `data` directory (relative to
where the java runtime was started from.  To customize the ES
behavior, edit the `config/elasticsearch.yml` file.  If you customize
the http host and port, be sure to update the Riemann and Kibana
configurations.

Kibana is a series of HTML and Javascript files served up from
`htdocs` and it's configuration is in `htdocs/kibana/config.js`.  We
serve up Kibana using an internal webserver, whose default port is
`9090` and will listen on all interfaces.

# Usage

	bin/kiries   # call with -? for cmd line args

And point your browser at <http://localhost:9090/index.html>
