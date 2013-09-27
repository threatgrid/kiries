## Kiries - KIbana, RIemann and ElasticSearch, glued together with Clojure

We wanted a point-and-shoot realtime trend analysis toolkit

We include the following versions of the third-party components

* Elastic Search 0.90.5
* Kibana 3.0.0milestone3
* Riemann 0.2.2

Those teams have all done incredible work!

Any clojure libraries or dependencies are described in the project.clj

## Installation

Unpack the archive, or open up the git repository, and you will see a
directory structure like this:

 * `config` -- configuration files for riemann and elasticsearch
 * `htdocs` -- Web documents, including kibana, and it configuration in `kabana/config.js`
 * `src` -- Kiries src files
 * `logs` -- log files
 * `bin` -- helper shell scripts
 * `lib` -- libraries and jars

__WARNING__ : Kiries will open several ports on your host, which have
no authentication or access control. The default ports are:

 * 9090 -- Webserver, serving up Kibana
 * 9200,9300 -- ElasticSearch HTTP and Native APIs
 * 5555 -- Riemann listeners (tcp and udp)

Read the following and take precautions as you see fit.

By default, Kiries will start up Riemann tcp and udp servers on port
`5555`.  It will index all events it recieves into ElasticSearch.  To
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

## Usage

	bin/kiries   # call with -? for cmd line args


