/**
 *  iCamera
 *
 *  Copyright 2017 Daniel Carter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
 // CHANGE LOG:
 // 05/11/2017 - Change number settings to "required: false" to workaround bug in Android app
 //            - Use state variables to hold boolean settings since the setting values seem to get misreported;
 //              this fixes a bug with motion triggered image capture not working when "False Alarm Prevention" is on
 // 05/11/2017 - Initial Release
 
metadata {
	definition (name: "iCamera", namespace: "karatecarter", author: "Daniel Carter") {
		capability "Configuration"
		capability "Image Capture"
		capability "Motion Sensor"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Switch"
		capability "Video Camera"
		capability "Video Capture"
        
        command "startVideo"
        command "alarmOn"
        command "alarmOff"
        command "triggerMotion"
        command "endMotion"
        command "setCallbackURL"
        command "window1On"
        command "window1Off"
        command "window2On"
        command "window2Off"
        command "window3On"
        command "window3Off"
        command "window4On"
        command "window4Off"
        command "reboot"
        
        attribute "window1Name", "string"
        attribute "window2Name", "string"
        attribute "window3Name", "string"
        attribute "window4Name", "string"
        attribute "window1Monitor", "string"
        attribute "window2Monitor", "string"
        attribute "window3Monitor", "string"
        attribute "window4Monitor", "string"
    }


	simulator {
		// TODO: define status and reply messages here
	}

	preferences {
    	input(title: "Camera URL", description: "Note: In order to stream live video from your camera, your phone will need to connect directly to the camera.  In most home networks this means that in order to access live video outside of your home network, the following address must point to your home router, and the appropriate port forwarding should be set up to send requests to the camera.  All other feature of the camera will still work from anywhere with a private address.", type: "paragraph", element: "paragraph")
        input("hostname", "string", title:"Camera IP Address/Hostname", required: true)
        input("httpport", "number", title:"Camera HTTP Port", defaultValue: "80" , required: false)  // using "required: false" to workaround Android bug
        input("rtspport", "number", title:"Camera RTSP Port", defaultValue: "554" , required: false) // using "required: false" to workaround Android bug
        input("username", "string", title:"Camera Username (Case Sensitive)", defaultValue: "administrator", description: "Camera Username (case sensitive)", required: true)
        input("password", "password", title:"Camera Password (Case Sensitive)", description: "Camera Password (case sensitive)", required: false)
        input(title: "Camera Video Stream", description: "Choose which type of video stream to use for live video from the camera.  RTSP includes sound, depending on camera configuration.", type: "paragraph", element: "paragraph")
        input("streamType", "enum", title:"Video Stream Type", defaultValue: "rtsp", options: ["rtsp": "RTSP", "mjpeg": "MJPEG"], required: true)
        input(title: "Motion Detection", description: "When motion is detected, the alarm will remain on until the motion has been inactive for this many seconds.", type: "paragraph", element: "paragraph")
        input("needMotionConfirmation", "bool", title: "False Alarm Prevention", description: "When motion is detected, the camera logs will be queried to prevent false alarms; this will add a slight delay in motion reporting and motion lasting less than 4 seconds or so may not be captured.", defaultValue: true, required: true)
        input("motionTimeout", "number", title: "End Motion After", defaultValue: "5", description: "Seconds", required: false) // using "required: false" to workaround Android bug
        input("motionDetectionPicture", "bool", title: "Take Pictures On Motion Detection", description: "You can choose to take one or more pictures when motion is detected by entering a number below, or enter 0 to continue taking pictures for as long as the motion persists.", defaultValue: "true", required: true)
        input("motionDetectionNumPictures", "number", title: "Number Of Pictures", description: "0 for continuous", defaultValue: "0")
        input("motionDetectionPictureInterval", "number", title: "Picture Interval (Seconds)", description: "Seconds", defaultValue: "30")
        input("motionDetectionEmail", "bool", title: "Send Email On Motion Detection", description: "This uses the camera's built in email functionality; configure email parameters through the camera software.", defaultValue: "false", required: true)
        input("setMotionWindow", "bool", title:"Motion Detection Window", description: "The camera supports up to four detection windows.  These windows can be configured through the camera software, but if this switch is on then one of the windows can be configured here.  The following Window settings will be ignored if this is off.", defaultValue: "false")
        input("motionWindowNum", "number", title: "Window Number (1-4)", defaultValue: "1")
        input("motionWindowName", "string", title: "Window Name", defaultValue: "Full Screen")
        input("motionWindowCoordinates", "string", title: "Window Coordinates", defaultValue: "96,240,543,479")
        input("motionWindowSensitivity", "number", title: "Window Sensitivity (0-10)", defaultValue: "6")
        input("motionWindowThreshhold", "number", title: "Window Threshold (0-255)", defaultValue: "20")
	}        
    
    tiles(scale: 2) {
		multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.camera", key: "CAMERA_STATUS") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "", backgroundColor: "#79b821", defaultState: true)
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "", backgroundColor: "#ffffff")
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-indoor", action: "", backgroundColor: "#F22000")
			}

			tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
				attributeState("errorMessage", label: "", value: "", defaultState: true)
			}

			tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
				attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821")
				attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff", defaultState: true)
				attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
				attributeState("unavailable", label: "Click here to connect", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
			}

            tileAttribute ("device.ledStatus", key: "SECONDARY_CONTROL") {
                attributeState "autoOn", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#79b821", nextState:"..."
                attributeState "autoOff", label: "Auto", action: "toggleLED", icon: "st.lights.multi-light-bulb-on", backgroundColor: "#9ceaf0", nextState:"..."
                attributeState "on", label: "On", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#79b821", nextState:"..."
                attributeState "off", label: "Off", action: "toggleLED", icon: "st.lights.multi-light-bulb-off", backgroundColor: "#FFFFFF", nextState:"..."
                attributeState "...", label: "...", action:"", nextState:"..."
            }

            tileAttribute("device.startLive", key: "START_LIVE") {
				attributeState("live", action: "startVideo", defaultState: true)
			}

			tileAttribute("device.stream", key: "STREAM_URL") {
				attributeState("activeURL", defaultState: true)
			}
		}
        
        carouselTile("cameraDetails", "device.image", width: 4, height: 2) { }

		standardTile("take", "device.image", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "take", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
			state "taking", label:'Taking', action: "", icon: "st.camera.take-photo", backgroundColor: "#53a7c0"
			state "image", label: "Take", action: "Image Capture.take", icon: "st.camera.camera", backgroundColor: "#FFFFFF", nextState:"taking"
		}
		
        standardTile("alarmStatus", "device.alarmStatus", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "off", label: "Off", action: "alarmOn", icon: "st.camera.dlink-indoor", backgroundColor: "#FFFFFF", nextState:"..."
            state "on", label: "On", action: "alarmOff", icon: "st.camera.dlink-indoor",  backgroundColor: "#79b821", nextState:"..."
            state "alarm", label: "Motion", action: "alarmOff", icon: "st.camera.dlink-indoor",  backgroundColor: "#53A7C0", nextState:""
            state "unavailable", label: "Offline", icon: "st.camera.dlink-indoor",  backgroundColor: "#ff0e05", nextState:""
            state "...", label: "...", action:"", icon: "st.camera.dlink-indoor", nextState:"..."
        }
		
        standardTile("window1Monitor", "device.window1Monitor", width: 1, height: 1) {
        	state "...", label: "...", icon: "st.Home.home9", backgroundColor: "#FFFFFF"
        	state "off", label: "Off", action: "window1On", icon: "st.Home.home9", nextState: "...", backgroundColor: "#FFFFFF"
            state "on", label: "On", action: "window1Off", icon: "st.Home.home9", nextState: "...", backgroundColor: "#79b821"
        }
		
        standardTile("window2Monitor", "device.window2Monitor", width: 1, height: 1) {
			state "...", label: "...", icon: "st.Home.home9", backgroundColor: "#FFFFFF"
        	state "off", label: "Off", action: "window2On", icon: "st.Home.home9", nextState: "...", backgroundColor: "#FFFFFF"
            state "on", label: "On", action: "window2Off", icon: "st.Home.home9", nextState: "...", backgroundColor: "#79b821"
        }
		
        standardTile("window3Monitor", "device.window3Monitor", width: 1, height: 1) {
			state "...", label: "...", icon: "st.Home.home9", backgroundColor: "#FFFFFF"
        	state "off", label: "Off", action: "window3On", icon: "st.Home.home9", nextState: "...", backgroundColor: "#FFFFFF"
            state "on", label: "On", action: "window3Off", icon: "st.Home.home9", nextState: "...", backgroundColor: "#79b821"
        }
		
        standardTile("window4Monitor", "device.window4Monitor", width: 1, height: 1) {
			state "...", label: "...", icon: "st.Home.home9", backgroundColor: "#FFFFFF"
        	state "off", label: "Off", action: "window4On", icon: "st.Home.home9", nextState: "...", backgroundColor: "#FFFFFF"
            state "on", label: "On", action: "window4Off", icon: "st.Home.home9", nextState: "...", backgroundColor: "#79b821"
        }
        
        valueTile("window1Name", "device.window1Name", width: 1, height: 1, decoration: "flat") {
        	state "default", label: '${currentValue}', backgroundColor: "#FFFFFF"
        }
        
        valueTile("window2Name", "device.window2Name", width: 1, height: 1, decoration: "flat") {
        	state "default", label: '${currentValue}', backgroundColor: "#FFFFFF"
        }
        
        valueTile("window3Name", "device.window3Name", width: 1, height: 1, decoration: "flat") {
        	state "default", label: '${currentValue}', backgroundColor: "#FFFFFF"
        }
        
        valueTile("window4Name", "device.window4Name", width: 1, height: 1, decoration: "flat") {
        	state "default", label: '${currentValue}', backgroundColor: "#FFFFFF"
        }
        
        standardTile("main", "device.alarmStatus", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "off", label: "Detect Off", action: "toggleAlarm", icon: "st.camera.dlink-indoor", backgroundColor: "#FFFFFF"
            state "on", label: "Detect On", action: "toggleAlarm", icon: "st.camera.dlink-indoor",  backgroundColor: "#79b821"
            state "alarm", label: "Motion", action: "toggleAlarm", icon: "st.camera.dlink-indoor",  backgroundColor: "#53A7C0"
            state "unavailable", label: "Offline", icon: "st.camera.dlink-indoor",  backgroundColor: "#ff0e05", nextState:""
            
        }
		
        standardTile("motion", "device.motion", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "inactive", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#FFFFFF"
            state "active", label: "Active", icon: "st.camera.dlink-indoor",  backgroundColor: "#53A7C0"
        }
        
        standardTile("configure", "device.configure", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
        }
        
        standardTile("refresh", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "refresh", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("reboot", "device.status", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
        	state "reboot", label: "Reboot", action:"reboot", icon:"st.motion.motion.inactive", backgroundColor: "#e7e7e7"
        }
        
        
		main("alarmStatus")
		details(["videoPlayer", "cameraDetails", "take", "alarmStatus", "window1Monitor", "window2Monitor", "window3Monitor", "window4Monitor", "window1Name", "window2Name", "window3Name", "window4Name", "configure", "refresh", "reboot"])
	}
}

mappings {
   path("/getInHomeURL") {
       action:
       [GET: "getInHomeURL"]
   }
}
private getLine(text, startPos)
{
	def eolChar = ""
    if (text.find("\r"))
    {
    	eolChar = "\r"
    } else if (text.find("\n"))
    {
    	eolChar = "\n"
    } else {
    	return(text.substring(startPos))
    }
    text = text.substring(startPos)
    return(text.substring(0, text.indexOf(eolChar)))
}

def installed() {
	log.debug "Installed with settings: $settings"
    
    // these boolean settings don't always seem to work right below
    state.motionDetectionPicture = motionDetectionPicture
    state.motionDetectionEmail = motionDetectionEmail
    
    state.waitingForConfirmation = false
    state.waitingForResponse = false
    configure()
}

def updated() {
	log.debug "Updated with settings: $settings"
    
    // these boolean settings don't always seem to work right below
    state.motionDetectionPicture = motionDetectionPicture
    state.motionDetectionEmail = motionDetectionEmail
    
    state.waitingForConfirmation = false
    state.waitingForResponse = false
    configure()
}
// parse events into attributes
def parse(String description) {
    log.trace "Received response from Camera to hubAction"
    
    def descMap = parseDescriptionAsMap(description)
    //log.trace "${descMap.inspect()}"
    
    // Check if its a picture and process it
	if (descMap["tempImageKey"]) {
		log.trace "Saving Image"
        state.waitingForResponse = false
    	unschedule("noResponse")
    	storeTemporaryImage(descMap.tempImageKey, getPictureName())
	} else if (descMap["headers"] && descMap["body"]) { // Otherwise check camera response
        def body = ""
        //def headers = ""
        try {
        	body = new String(descMap["body"].decodeBase64())
            //headers = new String(descMap["headers"].decodeBase64())
	    }
        catch(Exception e)
        {
        	log.warn "Error parsing response: $e"
        }
        //log.trace "Body -> ${body}"
		if (body != "") {
        	state.waitingForResponse = false
    		unschedule("noResponse")
    		//processResponse(headers, body)
            processResponse(body)
        }
       
	}
}

def processResponse(def body) {
	def sendEmail = ""
    def eventMTLine = ""
    def eventInterval = ""
    def window1Switch = ""
    def window2Switch = ""
    def window3Switch = ""
    def window4Switch = ""
    def windowMName = ""
    def window1Name = ""
    def window2Name = ""
    def window3Name = ""
    def window4Name = ""
    def window1Coordinates = ""
    def window1Sensitivity = ""
    def window1Threshold = ""
    
    // get title
    def title
    def startOfLine = 0
    if (body.find("<title>"))
    {
    	startOfLine = body.indexOf("<title>")
    }
    
    def firstLine = getLine(body, startOfLine)
    
    log.trace "Processing Response: ${firstLine}"
    
    if (body.find("401 Unauthorized")) {
        log.error "Camera responded with an 401 Unauthorized error. Check you Username and Password (BOTH are case sensitive). Error -> ${body}"
        return
    }
    
    if (firstLine.find("log.cgi"))
    {
        def dateAndTime = firstLine.substring(0, 20)
        log.trace "Searching log for ${dateAndTime}DEAMON: /usr/local/bin/stream_server"
        if (body.find("${dateAndTime}DEAMON: /usr/local/bin/stream_server")) // I think this indicates a "motion detected" line
        {
            confirmedMotion()
        } else {
        	log.trace "Not found"
            if (state.waitingForConfirmation) falseAlarm()
        }
    	return
    }
    
    if (body.find("event_mt="))
    {
    	eventMTLine = getLine(body, body.indexOf("event_mt="))
    }
    
    if(body.find("event_trigger=0") || eventMTLine.find("httpn:0")) {
        log.info("Polled: Motion Alarm Off")
        sendEvent(name: "alarmStatus", value: "off")
        sendEvent(name: "switch", value: "off", displayed: false)
    }
    else if(body.find("event_trigger=1") && body.find("httpn:1"))
    {
        log.info("Polled: Motion Alarm On")
        if (device.currentValue("alarmStatus") != "alarm")
        {
        	sendEvent(name: "alarmStatus", value: "on")
        }
        sendEvent(name: "switch", value: "on", displayed: false)
    }
            
    if(body.find("http_notify=0")) {
        log.info("Polled: Motion Alarm Callback Notification Disabled")
    } else if(body.find("http_notify=1") && body.find("http_url=")) {
        def callbackURL = getLine(body, body.indexOf("http_url=") + 9)
        log.info("Polled: Motion Alarm Callback Notification Enabled with URL $callbackURL")
    }
    
    if(eventMTLine.find("email:")) sendEmail = eventMTLine.substring(eventMTLine.indexOf("email:") + 6, eventMTLine.indexOf("email:") + 7)
    if(body.find("event_interval=")) eventInterval = getLine(body, body.indexOf("event_interval=") + 15)
    if(body.find("md_switch1=")) window1Switch = getLine(body, body.indexOf("md_switch1=") + 11)
    if(body.find("md_switch2=")) window2Switch = getLine(body, body.indexOf("md_switch2=") + 11)
    if(body.find("md_switch3=")) window3Switch = getLine(body, body.indexOf("md_switch3=") + 11)
    if(body.find("md_switch4=")) window4Switch = getLine(body, body.indexOf("md_switch4=") + 11)
    if(body.find("md_name1=")) window1Name = getLine(body, body.indexOf("md_name1=") + 9)
    if(body.find("md_name2=")) window2Name = getLine(body, body.indexOf("md_name2=") + 9)
    if(body.find("md_name3=")) window3Name = getLine(body, body.indexOf("md_name3=") + 9)
    if(body.find("md_name4=")) window4Name = getLine(body, body.indexOf("md_name4=") + 9)
    if(body.find("md_name${motionWindowNum}=")) windowMName = getLine(body, body.indexOf("md_name${motionWindowNum}=") + 9)
    if(body.find("md_window${motionWindowNum}=")) window1Coordinates = getLine(body, body.indexOf("md_window${motionWindowNum}=") + 11)
    if(body.find("md_sensitivity${motionWindowNum}=")) window1Sensitivity = getLine(body, body.indexOf("md_sensitivity${motionWindowNum}=") + 16)
    if(body.find("md_threshold${motionWindowNum}=")) window1Threshold = getLine(body, body.indexOf("md_threshold${motionWindowNum}=") + 14)
    
    if (window1Switch != "" && setMotionWindow && (windowMName != motionWindowName || window1Coordinates != motionWindowCoordinates || window1Sensitivity != "$motionWindowSensitivity" || window1Threshold != "$motionWindowThreshhold"))
    {
    	log.warn "Motion window parameters differ, calling Configure"
        configure()
        log.warn "$windowMName != $motionWindowName || $window1Coordinates != $motionWindowCoordinates || $window1Sensitivity != $motionWindowSensitivity || $window1Threshold != $motionWindowThreshhold"
    } else if (device.currentValue("switch") == "on" && sendEmail != "" && (sendEmail != (state.motionDetectionEmail ? "1":"0") || eventInterval != "0"))
    {
    	// not sure why motionDetectionEmail always seems to return the wrong value here
        log.warn "Event parameters differ; calling configure"
        log.warn "sendEmail=$sendEmail eventInterval=$eventInterval"
        log.warn "motionDetectionEmail=${state.motionDetectionEmail ? "1":"0"}"
        //configure()
        
	}
    
    if (window1Switch != "") sendEvent(name: "window1Monitor", value: (window1Switch == "1"? "on":"off"), descriptionText: "$window1Name is ${window1Switch == "1"? "":"not "}being monitored")
    if (window2Switch != "") sendEvent(name: "window2Monitor", value: (window2Switch == "1"? "on":"off"), descriptionText: "$window2Name is ${window2Switch == "1"? "":"not "}being monitored")
    if (window3Switch != "") sendEvent(name: "window3Monitor", value: (window3Switch == "1"? "on":"off"), descriptionText: "$window3Name is ${window3Switch == "1"? "":"not "}being monitored")
    if (window4Switch != "") sendEvent(name: "window4Monitor", value: (window4Switch == "1"? "on":"off"), descriptionText: "$window4Name is ${window4Switch == "1"? "":"not "}being monitored")
    
    if (window1Name != "") sendEvent(name: "window1Name", value: window1Name, displayed: false)
    if (window2Name != "") sendEvent(name: "window2Name", value: window2Name, displayed: false)
    if (window3Name != "") sendEvent(name: "window3Name", value: window3Name, displayed: false)
    if (window4Name != "") sendEvent(name: "window4Name", value: window4Name, displayed: false)

    
}

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private getStreamURL()
{
	if (streamType == "rtsp")
   	{
    	state.streamURL = "rtsp://${hostname}:${rtspport}/img/media.sav"
    } else {
    	state.streamURL = "http://${hostname}:${httpport}/img/video.mjpeg"
    }
    log.debug "Stream URL = ${state.streamURL}"
}

private getSnapshotURI()
{
	return "/img/snapshot.cgi"
}

private getHostAddress()
{
	return "${hostname}:${httpport}"
}

private getPictureName() {
	def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
	"image" + "_$pictureUuid" + ".jpg"
}

private getAuthorization() {
    def userpassascii = username + ":" + password
    "Basic " + userpassascii.encodeAsBase64().toString()
}

private hubGet(def uri) {
    if (!state.hostname)
    {
    	state.hostname = ""
    }
    
    if (state.hostname != hostname)
    {
    	state.hostname = hostname
        if (isIPAddress(hostname)) {
            state.ip = hostname
        } else {
        	state.ip = convertHostnameToIPAddress(hostname)
        }
    	log.debug "Setting camera IP to ${state.ip}"
    } else {
    	//log.debug "Camera IP is ${state.ip}"
    }
    
    //Need to set network id or parse() won't get called with results
    def iphex = convertIPtoHex(state.ip)
    def porthex = convertPortToHex(httpport)
    device.deviceNetworkId = "$iphex:$porthex"
    
    log.trace "Sending hubAction command -> http://${getHostAddress()}$uri"
    def hubAction = new physicalgraph.device.HubAction(
        method: "GET",
        path: uri,
        headers: [HOST:getHostAddress(),
                  Authorization:getAuthorization()]
    )
	// needed for pictures:
    if (uri == getSnapshotURI())
    {
    	hubAction.options = [outputMsgToS3:true]
    }
    sendHubCommand(hubAction)
    
    if (!state.waitingForResponse)
    {
    	log.trace "Check for response in 30 seconds"
        runIn(30, "noResponse") // if no response is received in 30 seconds then device will be marked offline
    	state.waitingForResponse = true
    }
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}

private boolean isIPAddress(String ipAddress)
{
    try
    {
         String[] parts = ipAddress.split("\\.")
         if (parts.length != 4) {
         	return false
         }
         for (int i = 0; i < 4; ++i)
         {
             int p = Integer.parseInt(parts[i])
             if (p > 255 || p < 0) {
             	return false
             }
         }
         return true
    } catch (Exception e)
    {
        return false
    }
}

private String convertHostnameToIPAddress(hostname) {
    def params = [
        uri: "http://dns.google.com/resolve?name=" + hostname,
        contentType: 'application/json'
    ]

    def retVal = null

    try {
        retVal = httpGet(params) { response ->
            log.trace "Request was successful, data=$response.data, status=$response.status"
            //log.trace "Result Status : ${response.data?.Status}"
            if (response.data?.Status == 0) { // Success
                for (answer in response.data?.Answer) { // Loop through results looking for the first IP address returned otherwise it's redirects
                    //log.trace "Processing response: ${answer}"
                    if (isIPAddress(answer?.data)) {
                        log.trace "Hostname ${answer?.name} has IP Address ${answer?.data}"
                        return answer?.data // We're done here (if there are more ignore it, we'll use the first IP address returned)
                    } else {
                        log.trace "Hostname ${answer?.name} redirected to ${answer?.data}"
                    }
                }
            } else {
                log.warn "DNS unable to resolve hostname ${response.data?.Question[0]?.name}, Error: ${response.data?.Comment}"
            }
        }
    } catch (Exception e) {
        log.warn("Unable to convert hostname to IP Address, Error: $e")
    }

    //log.trace "Returning IP $retVal for Hostname $hostname"
    return retVal
}

// Milli seconds delay between sending commands
private int delayInterval() {
 return 800
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
    getStreamURL()
    
    def cmds = []
    
    if (setMotionWindow)
    {
    	cmds << hubGet("/adm/set_group.cgi?group=MOTION&md_name${motionWindowNum}=${URLEncoder.encode(motionWindowName)}&md_window${motionWindowNum}=${motionWindowCoordinates}&md_sensitivity${motionWindowNum}=${motionWindowSensitivity}&md_threshold${motionWindowNum}=${motionWindowThreshhold}")
    }
    if (device.currentValue("switch") == "on")
    {
        cmds << hubGet("/adm/set_group.cgi?group=EVENT&event_interval=0&event_mt=email:${state.motionDetectionEmail ? "1" : "0"}")
    }
    
    cmds << poll()
    
    delayBetween(cmds, delayInterval())
}

def refresh()
{
	poll()
}

def on()
{
    alarmOn()
}

def off()
{
    alarmOff()
}

def alarmOn()
{
    log.debug "Enabling Alarm"

	// Not sure how to set h_trig_md ("Motion Detection" checkbox on the "Event Trigger" page) through API:
    delayBetween([hubGet("/adm/file.cgi?todo=save&h_en_trig=1&h_trig_md=1"), hubGet("/adm/set_group.cgi?group=EVENT&event_trigger=1&event_interval=0&event_mt=email:${state.motionDetectionEmail ? "1" : "0"};httpn:1"), pollGroup("EVENT")], delayInterval())
	// set event interval to 0 to allow camera to keep sending motion detection events while motion is active; motion will be ended when no events have been received for motionTimeout seconds
}

def alarmOff()
{
    log.debug "Disabling Alarm"

	// Not sure how to set h_trig_md ("Motion Detection" checkbox on the "Event Trigger" page) through API:
    delayBetween([hubGet("/adm/file.cgi?todo=save&h_en_trig=0&h_trig_md=0"), hubGet("/adm/set_group.cgi?group=EVENT&event_trigger=0&event_mt=email:0;httpn:0"), pollGroup("EVENT")], delayInterval())
}

def poll()
{
	log.trace "poll()"
    
    def cmds = []
    
    cmds << hubGet("/adm/get_group.cgi?group=EVENT") // for alarm (event_trigger & event_mt)
    cmds << hubGet("/adm/get_group.cgi?group=MOTION") // for motion window
    cmds << hubGet("/adm/get_group.cgi?group=HTTP_NOTIFY") // for motion window
    cmds << hubGet("/adm/log.cgi") // check for motion
    
    delayBetween(cmds, delayInterval())
}

def pollGroup(group)
{
	hubGet("/adm/get_group.cgi?group=$group")
}

def take() {
	log.info("${device.label} taking photo")

	hubGet(getSnapshotURI())
}

private takeMotionTriggeredPicture(force)
{
	if (device.currentValue("motion") == "active" || force)
    {
        take()
        state.numPicturesTaken = state.numPicturesTaken + 1
        if (motionDetectionNumPictures == state.numPicturesTaken)
        {
            log.trace "Took ${state.numPicturesTaken} pictures, done"
            return
        }
        runIn(motionDetectionPictureInterval, "takeMotionTriggeredPicture")
    }
}

def startVideo() {
	log.trace "streaming ${state.streamURL}"
	def dataLiveVideo = [
		OutHomeURL  : state.streamURL,
		InHomeURL   : state.streamURL,
		ThumbnnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
		cookie      : [key: "key", value: "value"]
	]

	def event = [
		name           : "stream",
		value          : groovy.json.JsonOutput.toJson(dataLiveVideo).toString(),
		data		   : groovy.json.JsonOutput.toJson(dataLiveVideo),
		descriptionText: "Starting the livestream",
		eventType      : "VIDEO",
		displayed      : false,
		isStateChange  : true
	]
	sendEvent(event)
}

def getInHomeURL() {
	 log.trace "Called getInHomeURL, returning $state.streamURL"
    [InHomeURL: state.streamURL]
}

def triggerMotion() {
	log.debug "Motion Triggered"
    
    if (needMotionConfirmation && device.currentValue("motion") != "active")
    {
    	if (!state.waitingForConfirmation)
        {
        	state.waitingForConfirmation = true
            hubGet("/adm/log.cgi")
            runIn(5, "falseAlarm") 
        }
    } else {
    	confirmedMotion()
    }
}

private confirmedMotion() {
	log.info "Motion Confirmed"
    unschedule("falseAlarm")
    state.waitingForConfirmation = false
    
	if (device.currentValue("motion") == "inactive")
    {
    	log.info "Motion is active"
        sendEvent(name: "motion", value: "active")
    	state.numPicturesTaken = 0
        if (state.motionDetectionPicture)
        {
        	log.trace "Capturing Image"
            takeMotionTriggeredPicture(true) // force first picture even if waiting for motionConfirmation
        } else {
        	log.debug "state.motionDetectionPicture = ${state.motionDetectionPicture}"
        }
    }
    sendEvent(name: "alarmStatus", value: "alarm", displayed: false)
    
    runIn(motionTimeout, "endMotion")
}

def endMotion() {
	log.info "Motion Ended"
    state.waitingForConfirmation = false
    sendEvent(name: "alarmStatus", value: device.currentValue("switch"), displayed: false)
    sendEvent(name: "motion", value: "inactive")
}

def setCallbackURL(String url)
{
	log.info "Setting callback URL"
    delayBetween([hubGet("/adm/set_group.cgi?group=HTTP_NOTIFY&http_notify=1&http_url=$url"), pollGroup("HTTP_NOTIFY")], delayInterval())
}

def window1On()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch1=1"), pollGroup("MOTION")], delayInterval())
}

def window1Off()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch1=0"), pollGroup("MOTION")], delayInterval())
}


def window2On()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch2=1"), pollGroup("MOTION")], delayInterval())
}

def window2Off()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch2=0"), pollGroup("MOTION")], delayInterval())
}


def window3On()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch3=1"), pollGroup("MOTION")], delayInterval())
}

def window3Off()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch3=0"), pollGroup("MOTION")], delayInterval())
}


def window4On()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch4=1"), pollGroup("MOTION")], delayInterval())
}

def window4Off()
{
	delayBetween([hubGet("/adm/set_group.cgi?group=MOTION&md_switch4=0"), pollGroup("MOTION")], delayInterval())
}

def reboot()
{
	runIn(5, "rebooted")
    hubGet("/adm/reboot.cgi")
}

def rebooted()
{
	log.info "Device Rebooted - Check for response"
    state.waitingForResponse = true
    pollGroup("EVENT")
    unschedule("noResponse")
    runIn(3, "noResponse", [overwrite: false])
}

private noResponse()
{
	log.warn "No response from camera"
    state.waitingForResponse = false
    state.waitingForConfirmation = false
    sendEvent(name: "alarmStatus", value: "unavailable", descriptionText: "Camera is offline")
    sendEvent(name: "switch", value: "unavailable", displayed: false)
    pollGroup("EVENT")
}

private falseAlarm()
{
	log.debug "False Alarm"
    unschedule("falseAlarm")
    state.waitingForConfirmation = false
}