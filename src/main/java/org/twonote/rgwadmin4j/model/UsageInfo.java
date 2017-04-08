package org.twonote.rgwadmin4j.model;

import org.twonote.rgwadmin4j.model.usage.Entries;
import org.twonote.rgwadmin4j.model.usage.Summary;

import java.util.List;

/*
Example:
{
   "entries":[
      {
         "user":"howdoyoudo",
         "buckets":[
            {
               "bucket":"howdoyoudo",
               "time":"2017-04-05 08:00:00.000000Z",
               "epoch":1491379200,
               "owner":"howdoyoudo",
               "categories":[
                  {
                     "category":"create_bucket",
                     "bytes_sent":171,
                     "bytes_received":0,
                     "ops":9,
                     "successful_ops":9
                  },
                  {
                     "category":"put_obj",
                     "bytes_sent":0,
                     "bytes_received":983040,
                     "ops":24,
                     "successful_ops":24
                  }
               ]
            }
         ]
      },
      {
         "user":"qqq",
         "buckets":[
            {
               "bucket":"",
               "time":"2017-04-05 08:00:00.000000Z",
               "epoch":1491379200,
               "owner":"qqq",
               "categories":[
                  {
                     "category":"list_buckets",
                     "bytes_sent":2961,
                     "bytes_received":0,
                     "ops":9,
                     "successful_ops":9
                  }
               ]
            }
         ]
      }
   ],
   "summary":[
      {
         "user":"howdoyoudo",
         "categories":[
            {
               "category":"create_bucket",
               "bytes_sent":171,
               "bytes_received":0,
               "ops":9,
               "successful_ops":9
            },
            {
               "category":"put_obj",
               "bytes_sent":0,
               "bytes_received":983040,
               "ops":24,
               "successful_ops":24
            }
         ],
         "total":{
            "bytes_sent":171,
            "bytes_received":983040,
            "ops":33,
            "successful_ops":33
         }
      },
      {
         "user":"qqq",
         "categories":[
            {
               "category":"list_buckets",
               "bytes_sent":2961,
               "bytes_received":0,
               "ops":9,
               "successful_ops":9
            }
         ],
         "total":{
            "bytes_sent":2961,
            "bytes_received":0,
            "ops":9,
            "successful_ops":9
         }
      }
   ]
}
 */
/** Represents the request bandwidth usage information. */
public class UsageInfo {
  private List<Entries> entries;
  private List<Summary> summary;

  public List<Entries> getEntries() {
    return entries;
  }

  public void setEntries(List<Entries> entries) {
    this.entries = entries;
  }

  public List<Summary> getSummary() {
    return summary;
  }

  public void setSummary(List<Summary> summary) {
    this.summary = summary;
  }
}
