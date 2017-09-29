(ns clj-vultr.core
  "Vultr API from Clojure."
  (:require [clj-http.lite.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as s]))

(defonce api-endpoint "https://api.vultr.com")

(defn ^:dynamic vultr-get
  "Calls GET on Vultr API using token. Token can be
omitted, which means API call does not require a token."
  ([path token params]
     (let [ret (client/get (str api-endpoint path)
                           {:headers (when token
                                       {"API-Key" token})
                            :query-params (:query-params params)})
           body (:body ret)]
       (when-not (s/blank? body)
         (json/read-str body))))
  ([path token] (vultr-get path token nil)))

(defn ^:dynamic vultr-post
  "Calls POST on Vultr API using token."
  [path token params]
  (let [ret (client/post (str api-endpoint path)
                         {:headers {"API-Key" token}
                          :form-params (:form-params params)})
        body (:body ret)]
    (when-not (s/blank? body)
      (json/read-str body))))

;;; Account

(defn account-info
  "Details about current account."
  [token]
  (vultr-get "/v1/account/info" token))

;;; Application

(defn app-list
  "Retrieves application list. These refer to applications that can be
launched when creating a Vultr VPS."
  [token]
  (vultr-get "/v1/app/list" token))

;;; API Key

(defn auth-info
  "Retrieve information about the current API key."
  [token]
  (vultr-get "/v1/auth/info" token))

;;; Backup

(defn backup-list
  "List all backups on the current account."
  [token]
  (vultr-get "/v1/backup/list" token))

;;; Block Storage
;;; FIXME: write it

;;; DNS

(defn dns-create-domain
  "Create a domain name in DNS. Returns nil."
  [token domain ip]
  (vultr-post "/v1/dns/create_domain" token {:form-params
                                             {"domain" domain
                                              "serverip" ip}}))

(defn dns-create-record
  "Add a DNS record."
  ([token domain name type data ttl priority]
     (vultr-post "/v1/dns/create_record" token {:form-params
                                                {"domain" domain
                                                 "name" name
                                                 "type" type
                                                 "data" data
                                                 "ttl"  ttl
                                                 "priority" priority}}))
  ([token domain name type data]
     (dns-create-record token domain name type data nil nil)))

(defn dns-delete-domain
  "Delete a domain name and all associated records."
  [token domain]
  (vultr-post "/v1/dns/delete_domain" token {:form-params {"domain" domain}}))

(defn dns-delete-record
  "Delete an individual DNS record. Get 'recordid' via (dns-list-records)."
  [token domain recordid]
  (vultr-post "/v1/dns/delete_record" token {:form-params
                                             {"domain" domain
                                              "RECORDID" recordid}}))

(defn dns-update-record
  "Update a DNS record."
  ([token domain recordid name type data ttl priority]
    (vultr-post "/v1/dns/update_record" token {:form-params
                                               {"domain" domain
                                                "RECORDID" recordid
                                                "name" name
                                                "type" type
                                                "data" data
                                                "ttl"  ttl
                                                "priority" priority}}))
  ([token domain recordid name type data]
     (dns-update-record token domain recordid name type data nil nil)))

(defn dns-list
  "List all domains associated with the current account."
  [token]
  (vultr-get "/v1/dns/list" token))

(defn dns-list-records
  "List all the records associated with a particular domain."
  [token domain]
  (vultr-get "/v1/dns/records" token {:query-params {"domain" domain}}))

;;; ISO Image

(defn iso-create-from-url
  "Create a new ISO image on the current account. The ISO image will be downloaded
from a given URL. Download status can be checked with (iso-list) call."
  [token url]
  (vultr-post "/v1/iso/create_from_url" token {:form-params
                                               {"url" url}}))

(defn iso-list
  "List all ISOs currently available on this account."
  [token]
  (vultr-get "/v1/iso/list" token))

;;; Operating System

(defn os-list
  "Returns a list of available OS-es."
  []
  (vultr-get "/v1/os/list" nil))

;;; Plans

(defn plans-list
  "Retrieve a list of all active plans. Plans that are no longer available
will not be shown.

If 'type' was given, returns type of plans. Possible values:
\"all\", \"vc2\", \"ssd\", \"vdc2\", \"dedicated\"."
  ([type]
     (vultr-get "/v1/plans/list" nil {:query-params {"type" type}}))
  ([] (plans-list nil)))

(defn plans-list-vc2
  "Retrieve a list of all active vc2 plans. Plans that are no longer available
will not be shown."
  []
  (vultr-get "/v1/plans/list_vc2" nil))

(defn plans-list-vdc2
  "Retrieve a list of all active vdc2 plans. Plans that are no longer available
will not be shown."
  []
  (vultr-get "/v1/plans/list_vdc2" nil))

;;; Regions

(defn regions-availability
  "Retrieve a list of the VPSPLANIDs currently available in this location.
To see available dcid's, use (regions-list).

If 'type' was given, returns type of plans. Possible values:
\"all\", \"vc2\", \"ssd\", \"vdc2\", \"dedicated\"."
  ([dcid type]
     (vultr-get "/v1/regions/availability" nil {:query-params {"DCID" dcid "type" type}}))
  ([dcid] (regions-availability dcid nil)))

(defn regions-availability-vc2
  "Retrieve a list of the vdc2 VPSPLANIDs currently available in this location."
  [dcid]
  (vultr-get "/v1/regions/availability_vc2" nil {:query-params {"DCID" dcid}}))

(defn regions-availability-vcs2
  "Retrieve a list of the vdc2 VPSPLANIDs currently available in this location."
  [dcid]
  (vultr-get "/v1/regions/availability_vdc2" nil {:query-params {"DCID" dcid}}))

(defn regions-list
  "Retrieve a list of all active regions. Note that just because a region is listed
here, does not mean that there is room for new servers."
  []
  (vultr-get "/v1/regions/list" nil))

;;; Reserved IP
;;; FIXME: implement it

;;; Server

(defn server-app-change
  "Changes the virtual machine to a different application. All data will be permanently lost.
'subid' is unique identifier for this subscription. These can be found using the (server-list) call.
'appid' is application identifier to use. See (server-app-change-list)."
  [token subid appid]
  (vultr-post "/v1/server/app_change" token {:form-params
                                             {"SUBID" subid
                                              "APPID" appid}}))

(defn server-app-change-list
  "Retrieves a list of applications to which a virtual machine can be changed.
'subid' is unique identifier for this subscription. These can be found using the (server-list) call."
  [token subid]
  (vultr-post "/v1/server/app_change_list" token {:form-params
                                                  {"SUBID" subid}}))

(defn server-list
  "List of servers for given account token."
  [token]
  (vultr-get "/v1/server/list" token))

(defn server-backup-disable
  "Disables automatic backups on a server.
Once disabled, backups can only be enabled again by customer support."
  [token subid]
  (vultr-post "/v1/server/backup_disable" token {:form-params
                                                 {"SUBID" subid}}))

(defn server-backup-enable
  "Enables automatic backups on a server."
  [token subid]
  (vultr-post "/v1/server/backup_enable" token {:form-params
                                                {"SUBID" subid}}))

(defn server-backup-get-schedule
  "Retrieves the backup schedule for a server. All time values are in UTC."
  [token subid]
  (vultr-post "/v1/server/backup_get_schedule" token {:form-params
                                                      {"SUBID" subid}}))

;; FIXME: backup-set-schedule

(defn server-bandwidth
  "Get the bandwidth used by a virtual machine."
  [token subid]
  (vultr-get "/v1/server/bandwidth" token {:query-params 
                                           {"SUBID" subid}}))

(defn server-create
  "Create a new virtual machine. You will start being billed for this immediately.
The response only contains the SUBID for the new machine.

You should use (server-list) to poll and wait for the machine to be created (as
this does not happen instantly).

dcid is location to create this virtual machine in.  See (regions-list).
vpsplanid is plan to use when creating this virtual machine. See (plans-list).
osid is operating system to use. See (os-list).

Below are optional keys inside 'options' map and they can be either keyword/string and
uppercased or lowercased.

ipxe_chain_url string (optional) If you've selected the 'custom' operating system, this can be set to chainload the specified URL on bootup, via iPXE.
ISOID string (optional)  If you've selected the 'custom' operating system, this is the ID of a specific ISO to mount during the deployment
SCRIPTID integer (optional) If you've not selected a 'custom' operating system, this can be the SCRIPTID of a startup script to execute on boot.  See v1/startupscript/list
SNAPSHOTID string (optional) If you've selected the 'snapshot' operating system, this should be the SNAPSHOTID (see (snapshot-list)) to restore for the initial installation
enable_ipv6 string (optional) 'yes' or 'no'. If yes, an IPv6 subnet will be assigned to the machine (where available)
enable_private_network string (optional) 'yes' or 'no'. If yes, private networking support will be added to the new server.
label string (optional) This is a text label that will be shown in the control panel
SSHKEYID string (optional) List of SSH keys to apply to this server on install (only valid for Linux/FreeBSD).  See (sshkey-list).  Separate keys with commas
auto_backups string (optional) 'yes' or 'no'.  If yes, automatic backups will be enabled for this server (these have an extra charge associated with them)
APPID integer (optional) If launching an application (OSID 186), this is the APPID to launch. See (app-list).
userdata string (optional) Base64 encoded user-data
notify_activate string (optional, default 'yes') 'yes' or 'no'. If yes, an activation email will be sent when the server is ready.
ddos_protection (optional, default 'no') 'yes' or 'no'.  If yes, DDOS protection will be enabled on the subscription (there is an additional charge for this)
reserved_ip_v4 string (optional) IP address of the floating IP to use as the main IP of this server
hostname string (optional) The hostname to assign to this server.
tag string (optional) The tag to assign to this server.
FIREWALLGROUPID string (optional) The firewall group to assign to this server. See (firewall-group-list)."
  [token dcid vpsplanid osid options]
  (vultr-post "/v1/server/create" token {:form-params
                                         (merge 
                                          {"DCID" dcid
                                           "VPSPLANID" vpsplanid
                                           "OSID" osid}
                                          ;; make sure keys are uppercased strings
                                          (reduce-kv (fn [mp k v]
                                                       (assoc mp (-> k name .toUpperCase) (str v)))
                                                     {}
                                                     options))}))

(defn server-destroy
  "Destroy (delete) a virtual machine. All data will be permanently lost, and the IP address
will be released. There is no going back from this call."
  [token subid]
  (vultr-post "/v1/server/destroy" token {:form-params
                                          {"SUBID" subid}}))

;;; Snapshot

(defn snapshot-create
  "Create a snapshot from an existing virtual machine. The virtual machine does not need to be stopped."
  [token subid]
  (vultr-post "/v1/snapshot/create" token {:form-params
                                           {"SUBID" subid}}))

(defn snapshot-destroy
  "Destroy (delete) a snapshot. There is no going back from this call."
  [token snapshotid]
  (vultr-post "/v1/snapshot/destroy" token {:form-params
                                            {"SNAPSHOTID" snapshotid}}))

(defn snapshot-list
  "List all snapshots on the current account."
  [token]
  (vultr-get "/v1/snapshot/list" token))
