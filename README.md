# mixpanel-export

Provides `mixpanel-export.core/export` to export events from Mixpanel for a given time range.


## Configuration

Set the environment variables `MIXPANEL_API_KEY` and `MIXPANEL_API_SECRET` prior to requiring the core namespace, or rebind `mixpanel-export.core/*api-key*` and `mixpanel-export.core/*api-secret*`.  You can set the timeout by binding `mixpanel-export.core/*expiration-seconds*` (default 120). 


## Usage

```clj

    (require '[mixpanel-export.core :refer [export]])
    (require '[clj-time.core :as t])

    (def now (t/now))
    (def then (t/minus now (t/months 1)))
    (def events (export ["my event"] then now))

```


## License

Copyright © 2016 Curiosity, Inc

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
