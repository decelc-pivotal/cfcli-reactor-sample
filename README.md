# cfcli-reactor-sample

Add the api host, username and password to the application.properties file


From PAS tile credentials - uaa.admin_client_credentials

Example

```
{
   "credential": {
      "type": "simple_credentials",
      "value": {
          "identity": "admin",
          "password": "tmmcWkbuYRip6va1_bmJYxJqtWUeS_tb"
      }
   }
}
```

There are 2 endpoints

/listUsers

Return full details on all users, i.e. permissions, userName, id, etc...

/listClients

Return full details on all clients, i.e. permissions, userName, id, etc...
