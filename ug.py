#!/usr/bin/env python

# Geen idee hoe dit te gebruiken?
# 1. Download Python (als je dat nog niet hebt)
# 2. Open de commandline
# 3. ga naar directory met ug.py
# 4. start met 'python ug.py' (tv moet aanstaan!)
# 5. Ga naar Video menu met de home knop (zie YouTube filmpje voor verder gebruik)

import re
import os
import cookielib
import urllib2
import subprocess
import threading
import socket
import time
import sys
import uuid
from xml.sax.saxutils import escape
from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

# makkelijkere manier om eigen IP op te halen?
s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) 
s.connect(('google.com', 80))
ip = s.getsockname()[0]
s.close()

port = 8200 #port used to start 'DLNA' server
web = urllib2.build_opener(urllib2.HTTPCookieProcessor(cookielib.CookieJar())) #cookie enabled url opener
uuid = 'a752300c-fc74-11de-9978-001ec2aca9f9'

# zoek de MMS:// url die bij een be
def get_mms(id):
    web.open('http://player.omroep.nl/?aflID=%s' % id) # voor de cookie

    js = web.open('http://player.omroep.nl/js/initialization.js.php?aflID=%s' % id).read()
    securitycode = re.match('var securityCode = \'(.*?)\'', js).group(1); #haal securitycode op

    xml = web.open('http://player.omroep.nl/xml/metaplayer.xml.php?aflID=%s&md5=%s' % (id, securitycode)).read()
    streamfile = re.search('<stream compressie_kwaliteit=\'bb\' compressie_formaat=\'wmv\'>(.*?)</stream>', xml, re.DOTALL).group(1)
    stream = web.open(streamfile).read()
    mms = re.search('"(mms://.*?)"', stream, re.DOTALL).group(1)
    return mms

# Headers om een video aan te kondigen
def headers(s):
    s.send_response(200)
    s.send_header('Content-Type', 'video/mpeg')
    s.send_header('Content-Length', '5819801600')
    s.send_header('transferMode.dlna.org', 'Streaming')
    s.send_header('Accept-Ranges', 'bytes')
    s.send_header('Connection', 'Close')
    s.send_header('EXT', '')
    s.send_header('contentFeatures.dlna.org', 'DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=01;DLNA.ORG_CI=0')
    s.end_headers()

class MyHandler(BaseHTTPRequestHandler):
    def address_string(self):
        host, port = self.client_address[:2]
        return host

    def do_GET(self):
    	# Responses voor bestanden dia Bravia wil zien,
    	# Ben er nog niet achter wat er in staat
    	if self.path == '/rootDesc.xml':
    	    content = '<?xml version="1.0"?><root xmlns="urn:schemas-upnp-org:device-1-0"><specVersion><major>1</major><minor>0</minor></specVersion><device><deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType><friendlyName>Uitzending Gemist</friendlyName><manufacturer>Justin Maggard</manufacturer><manufacturerURL>http://www.debian.org/</manufacturerURL><modelDescription>MiniDLNA on Ubuntu</modelDescription><modelName>Windows Media Connect compatible (MiniDLNA)</modelName><modelNumber>1</modelNumber><modelURL>http://www.debian.org/</modelURL><serialNumber>12345678</serialNumber><UDN>uuid:'+uuid+'</UDN><dlna:X_DLNADOC xmlns:dlna="urn:schemas-dlna-org:device-1-0">DMS-1.50</dlna:X_DLNADOC><presentationURL>http://'+ip+'/</presentationURL><iconList><icon><mimetype>image/png</mimetype><width>48</width><height>48</height><depth>24</depth><url>/icons/sm.png</url></icon><icon><mimetype>image/png</mimetype><width>120</width><height>120</height><depth>24</depth><url>/icons/lrg.png</url></icon><icon><mimetype>image/jpeg</mimetype><width>48</width><height>48</height><depth>24</depth><url>/icons/sm.jpg</url></icon><icon><mimetype>image/jpeg</mimetype><width>120</width><height>120</height><depth>24</depth><url>/icons/lrg.jpg</url></icon></iconList><serviceList><service><serviceType>urn:schemas-upnp-org:service:ContentDirectory:1</serviceType><serviceId>urn:upnp-org:serviceId:ContentDirectory</serviceId><controlURL>/ctl/ContentDir</controlURL><eventSubURL>/evt/ContentDir</eventSubURL><SCPDURL>/ContentDir.xml</SCPDURL></service><service><serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType><serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId><controlURL>/ctl/ConnectionMgr</controlURL><eventSubURL>/evt/ConnectionMgr</eventSubURL><SCPDURL>/ConnectionMgr.xml</SCPDURL></service><service><serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType><serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId><controlURL>/ctl/X_MS_MediaReceiverRegistrar</controlURL><eventSubURL>/evt/X_MS_MediaReceiverRegistrar</eventSubURL><SCPDURL>/X_MS_MediaReceiverRegistrar.xml</SCPDURL></service></serviceList></device></root>'
            self.send_response(200)
            self.send_header('Content-Type', 'text/xml')
            self.send_header('Connection', 'close');
            self.send_header('Content-Length', len(content));
            self.end_headers()
    	    self.wfile.write(content)
            return
            	
        if self.path == '/ContentDir.xml':
            content = '<?xml version="1.0"?><scpd xmlns="urn:schemas-upnp-org:service-1-0"><specVersion><major>1</major><minor>0</minor></specVersion><actionList><action><name>GetSearchCapabilities</name><argumentList><argument><name>SearchCaps</name><direction>out</direction><relatedStateVariable>SearchCapabilities</relatedStateVariable></argument></argumentList></action><action><name>GetSortCapabilities</name><argumentList><argument><name>SortCaps</name><direction>out</direction><relatedStateVariable>SortCapabilities</relatedStateVariable></argument></argumentList></action><action><name>GetSystemUpdateID</name><argumentList><argument><name>Id</name><direction>out</direction><relatedStateVariable>SystemUpdateID</relatedStateVariable></argument></argumentList></action><action><name>Browse</name><argumentList><argument><name>ObjectID</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable></argument><argument><name>BrowseFlag</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_BrowseFlag</relatedStateVariable></argument><argument><name>Filter</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Filter</relatedStateVariable></argument><argument><name>StartingIndex</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Index</relatedStateVariable></argument><argument><name>RequestedCount</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>SortCriteria</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_SortCriteria</relatedStateVariable></argument><argument><name>Result</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable></argument><argument><name>NumberReturned</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>TotalMatches</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>UpdateID</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_UpdateID</relatedStateVariable></argument></argumentList></action><action><name>Search</name><argumentList><argument><name>ContainerID</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_ObjectID</relatedStateVariable></argument><argument><name>SearchCriteria</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_SearchCriteria</relatedStateVariable></argument><argument><name>Filter</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Filter</relatedStateVariable></argument><argument><name>StartingIndex</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Index</relatedStateVariable></argument><argument><name>RequestedCount</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>SortCriteria</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_SortCriteria</relatedStateVariable></argument><argument><name>Result</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable></argument><argument><name>NumberReturned</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>TotalMatches</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Count</relatedStateVariable></argument><argument><name>UpdateID</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_UpdateID</relatedStateVariable></argument></argumentList></action></actionList><serviceStateTable><stateVariable sendEvents="yes"><name>TransferIDs</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_ObjectID</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_Result</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_SearchCriteria</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_BrowseFlag</name><dataType>string</dataType><allowedValueList><allowedValue>BrowseMetadata</allowedValue><allowedValue>BrowseDirectChildren</allowedValue></allowedValueList></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_Filter</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_SortCriteria</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_Index</name><dataType>ui4</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_Count</name><dataType>ui4</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_UpdateID</name><dataType>ui4</dataType></stateVariable><stateVariable sendEvents="no"><name>SearchCapabilities</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>SortCapabilities</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="yes"><name>SystemUpdateID</name><dataType>ui4</dataType></stateVariable></serviceStateTable></scpd>'
            self.send_response(200)
            self.send_header('Content-Type', 'text/xml')
            self.send_header('Connection', 'close');
            self.send_header('Content-Length', len(content));
            self.end_headers()
            self.wfile.write(content)
            return
            
        if self.path == '/ConnectionMgr.xml':
            content = '<?xml version="1.0"?><scpd xmlns="urn:schemas-upnp-org:service-1-0"><specVersion><major>1</major><minor>0</minor></specVersion><actionList><action><name>GetProtocolInfo</name><argumentList><argument><name>Source</name><direction>out</direction><relatedStateVariable>SourceProtocolInfo</relatedStateVariable></argument><argument><name>Sink</name><direction>out</direction><relatedStateVariable>SinkProtocolInfo</relatedStateVariable></argument></argumentList></action><action><name>GetCurrentConnectionIDs</name><argumentList><argument><name>ConnectionIDs</name><direction>out</direction><relatedStateVariable>CurrentConnectionIDs</relatedStateVariable></argument></argumentList></action><action><name>GetCurrentConnectionInfo</name><argumentList><argument><name>ConnectionID</name><direction>in</direction><relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable></argument><argument><name>RcsID</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_RcsID</relatedStateVariable></argument><argument><name>AVTransportID</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_AVTransportID</relatedStateVariable></argument><argument><name>ProtocolInfo</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_ProtocolInfo</relatedStateVariable></argument><argument><name>PeerConnectionManager</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_ConnectionManager</relatedStateVariable></argument><argument><name>PeerConnectionID</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable></argument><argument><name>Direction</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_Direction</relatedStateVariable></argument><argument><name>Status</name><direction>out</direction><relatedStateVariable>A_ARG_TYPE_ConnectionStatus</relatedStateVariable></argument></argumentList></action></actionList><serviceStateTable><stateVariable sendEvents="yes"><name>SourceProtocolInfo</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="yes"><name>SinkProtocolInfo</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="yes"><name>CurrentConnectionIDs</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_ConnectionStatus</name><dataType>string</dataType><allowedValueList><allowedValue>OK</allowedValue><allowedValue>ContentFormatMismatch</allowedValue><allowedValue>InsufficientBandwidth</allowedValue><allowedValue>UnreliableChannel</allowedValue><allowedValue>Unknown</allowedValue></allowedValueList></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_ConnectionManager</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_Direction</name><dataType>string</dataType><allowedValueList><allowedValue>Input</allowedValue><allowedValue>Output</allowedValue></allowedValueList></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_ProtocolInfo</name><dataType>string</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_ConnectionID</name><dataType>i4</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_AVTransportID</name><dataType>i4</dataType></stateVariable><stateVariable sendEvents="no"><name>A_ARG_TYPE_RcsID</name><dataType>i4</dataType></stateVariable></serviceStateTable></scpd>'
            self.send_response(200)
            self.send_header('Content-Type', 'text/xml')
            self.send_header('Connection', 'close');
            self.send_header('Content-Length', len(content));
            self.end_headers()
            self.wfile.write(content)
            return
           
        # Oranje icoontje :)
        if self.path == '/icons/lrg.jpg':
            self.send_response(200)
            self.send_header('Content-Type', 'image/jpeg')
            self.end_headers()
            f = open('ug.jpg')
            self.wfile.write(f.read())
            f.close()
            return

    	#
    	# Ander bestand betekent altijd dat er een programma bekeken wil worden
    	#
    	
        programmaid = re.search('/MediaItems/(.*?).mpg', self.path).group(1)

        #create new output
        f = open('output.mpg', 'w')
        f.write('')
        f.close()

        headers(self)
        
        mms = get_mms(programmaid)
        command = 'mencoder %s -ovc lavc -lavcopts vcodec=mpeg2video:vbitrate=8000 -oac mp3lame -ofps 25 -of mpeg -mc 0 -o ./output.mpg' % mms.replace('&', '\&')
        proc = subprocess.Popen(command, shell=True)
        
        time.sleep(5) #Give mencoder some buffer time
        
        # Stream the inhoud van 'output.mpg' zo realtime mogelijk
        try:
            lastsize = 0
            while 1:
                size=os.path.getsize('output.mpg')
                if size > lastsize:
                    # Steeds opnieuw openen om laatst geschreven gegevens van mencoder te kunnen lezen
                    f = open('output.mpg', 'rb')
                    f.seek(lastsize)
                    part = f.read(size-lastsize)
                    f.close()
                    self.wfile.write(part)
                    self.wfile.flush()
                else:
                    time.sleep(0.2)
                lastsize = size
        except:
            os.kill(proc.pid, 9)
            print 'mencoder killed'

    def do_HEAD(self):
        headers(self)
        
    def do_POST(self):
        time.sleep(0.2)
        self.send_response(200)
        #self.send_header('Content-Type', 'text/xml; charset="utf-8"')
        #self.end_headers()

        if self.path == '/ctl/ContentDir':
            request = self.rfile.read(int(self.headers.getheader('content-length')))
            objectid = re.search('<ObjectID>(.*?)</ObjectID>', request)
            
            if objectid:
                objectid = objectid.group(1)
                
                
                # Inhoud van root map (alle dagen)
                if objectid == '0':
                    self.wfile.write('<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:BrowseResponse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1"><Result>&lt;DIDL-Lite xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/" xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/"&gt;&lt;container id="1" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Vandaag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="2" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Gisteren&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="3" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Maandag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="4" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Dinsdag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="5" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Woensdag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="6" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Donderdag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="7" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Vrijdag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="8" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Zaterdag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;container id="9" parentID="0" restricted="1" childCount="1"&gt;&lt;dc:title&gt;Zondag&lt;/dc:title&gt;&lt;upnp:class&gt;object.container.storageFolder&lt;/upnp:class&gt;&lt;/container&gt;&lt;/DIDL-Lite&gt;</Result><NumberReturned>4</NumberReturned><TotalMatches>4</TotalMatches><UpdateID>1</UpdateID></u:BrowseResponse></s:Body></s:Envelope>')
                    return
                    
                dag = {'1': 'vandaag',
                 '2': 'gisteren',
                 '3': '1',
                 '4': '2',
                 '5': '3',
                 '6': '4',
                 '7': '5',
                 '8': '6',
                 '9': '0'
                 }[objectid]
                
                web.open('http://www.uitzendinggemist.nl/')
                w = web.open('http://www.uitzendinggemist.nl/index.php/selectie?searchitem=dag&dag=%s&pgNum=0' % dag).read()
                w = w.decode('iso-8859-1').encode('utf8')
                
                programmas = re.compile('<tr.*?<a class="title" .*?>(.*?)</a></td>.*?<a href="http://player.omroep.nl/\?aflID=(.*?)" target="player".*?</tr>', re.DOTALL).findall(w)

                # Trim eerste programmas eraf als dat nodig is (voor programmalijsten langer dan ~20 programmas)
                startindex = int(re.search('<StartingIndex>(.*?)</StartingIndex>', request).group(1))
                programmas = programmas[startindex:] 

                xml = '<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:BrowseResponse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1"><Result>&lt;DIDL-Lite xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/" xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/"&gt;'
                
                for p in programmas:
                    xml += '&lt;item id="%s$%s" parentID="%s" restricted="1"&gt;&lt;dc:title&gt;%s&lt;/dc:title&gt;&lt;upnp:class&gt;object.item.videoItem&lt;/upnp:class&gt;&lt;dc:date&gt;2000-01-01T00:00:00&lt;/dc:date&gt;&lt;res size="58198016" duration="3:00:00.000" bitrate="195269" resolution="320x180" protocolInfo="http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=01;DLNA.ORG_CI=0"&gt;http://%s:8200/MediaItems/%s.mpg&lt;/res&gt;&lt;/item&gt;' % (objectid, p[1], objectid, escape(p[0]), ip, p[1])
                              
                xml += '&lt;/DIDL-Lite&gt;</Result><NumberReturned>%s</NumberReturned><TotalMatches>%s</TotalMatches><UpdateID>3</UpdateID></u:BrowseResponse></s:Body></s:Envelope>' % (len(programmas), len(programmas))

                self.wfile.write(xml)
                
            else:
                content = '<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:GetSortCapabilitiesResponse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1"><SortCaps>dc:title,dc:date,upnp:class,upnp:originalTrackNumber</SortCaps></u:GetSortCapabilitiesResponse></s:Body></s:Envelope>'
                self.send_header('Content-Type', 'text/xml; charset="utf-8"')
                self.send_header('Connection', 'close');
                self.send_header('Content-Length', len(content));
                self.end_headers()
                self.wfile.write(content)
    
        if self.path == '/ctl/ConnectionMgr':
            content = '<?xml version="1.0" encoding="utf-8"?><s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/"><s:Body><u:GetProtocolInfoResponse xmlns:u="urn:schemas-upnp-org:service:ConnectionManager:1"><Source>http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_HD_NA_ISO;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_SD_NA_ISO;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_MP_SD_AAC_MULT5;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_MP_SD_AC3;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=MPEG_TS_HD_NA;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/vnd.dlna.mpeg-tts:DLNA.ORG_PN=AVC_TS_MP_HD_AC3_T;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL;DLNA.ORG_OP=01;DLNA.ORG_CI=0,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL;DLNA.ORG_OP=01,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNA.ORG_PN=AAC_ISO_320;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNA.ORG_PN=AAC_ISO;DLNA.ORG_OP=01,http-get:*:audio/mp4:DLNA.ORG_PN=AAC_MULT5_ISO;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=8000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=8000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=11025;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=11025;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=12000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=12000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=16000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=16000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=22050;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=22050;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=24000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=24000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=32000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=32000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=44100;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=48000;channels=1:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:audio/L16;rate=48000;channels=2:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01,http-get:*:image/jpeg:*,http-get:*:video/avi:*,http-get:*:video/divx:*,http-get:*:video/x-matroska:*,http-get:*:video/mpeg:*,http-get:*:video/mp4:*,http-get:*:video/x-ms-wmv:*,http-get:*:video/x-msvideo:*,http-get:*:audio/mp4:*,http-get:*:audio/wav:*,http-get:*:audio/x-flac:*,http-get:*:application/ogg:*</Source><Sink></Sink></u:GetProtocolInfoResponse></s:Body></s:Envelope>'
            self.send_header('Content-Type', 'text/xml; charset="utf-8"')
            self.send_header('Connection', 'close');
            self.send_header('Content-Length', len(content));
            self.wfile.write(content)


class UDPThread(threading.Thread):
   def run(self):
       time.sleep(2)
    
       UDPSock = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
       addr = ('239.255.255.250',1900)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:uuid:'+uuid+'\r\nUSN:uuid:'+uuid+'\r\nNTS:ssdp:byebye\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:upnp:rootdevice\r\nUSN:uuid:'+uuid+'::upnp:rootdevice\r\nNTS:ssdp:byebye\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:urn:schemas-upnp-org:device:MediaServer:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:device:MediaServer:1\r\nNTS:ssdp:byebye\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:urn:schemas-upnp-org:service:ContentDirectory:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:service:ContentDirectory:1\r\nNTS:ssdp:byebye\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:urn:schemas-upnp-org:service:ConnectionManager:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:service:ConnectionManager:1\r\nNTS:ssdp:byebye\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nNT:urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\r\nUSN:uuid:'+uuid+'::urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\r\nNTS:ssdp:byebye\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:uuid:'+uuid+'\r\nUSN:uuid:'+uuid+'\r\nNTS:ssdp:alive\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:upnp:rootdevice\r\nUSN:uuid:'+uuid+'::upnp:rootdevice\r\nNTS:ssdp:alive\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:urn:schemas-upnp-org:device:MediaServer:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:device:MediaServer:1\r\nNTS:ssdp:alive\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:urn:schemas-upnp-org:service:ContentDirectory:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:service:ContentDirectory:1\r\nNTS:ssdp:alive\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:urn:schemas-upnp-org:service:ConnectionManager:1\r\nUSN:uuid:'+uuid+'::urn:schemas-upnp-org:service:ConnectionManager:1\r\nNTS:ssdp:alive\r\n\r\n', addr)
       UDPSock.sendto('NOTIFY * HTTP/1.1\r\nHOST:239.255.255.250:1900\r\nCACHE-CONTROL:max-age=1810\r\nLOCATION:http://'+ip+':'+str(port)+'/rootDesc.xml\r\nSERVER: Ubuntu/9.04 DLNADOC/1.50 UPnP/1.0 MiniDLNA/1.0\r\nNT:urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\r\nUSN:uuid:'+uuid+'::urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1\r\nNTS:ssdp:alive\r\n\r\n',addr)
       UDPSock.close()


t = UDPThread()
t.start()

print 'Uitzending Gemist server gestart'
print 'Zorg ervoor dat voordat u dit opstart uw TV aan/standby staat'
print
print 'Als u hieronder POST/GET regels ziet verschijnen heeft uw TV ons gedetecteerd'
print 'Zoek na detectie in het HOME>Video menu naar \'Uitzending Gemist\''
server = HTTPServer(('', port), MyHandler)
try:

    server.serve_forever()
except:
    print 'Afsluiten...'
