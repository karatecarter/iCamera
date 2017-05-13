/**
*  iCamera Connect and Motion Monitor
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
 // 05/11/2017 - Use try/catch when registering callback to avoid exception when creating devices
 // 05/11/2017 - Initial Release

definition(
    name: "iCamera Connect and Motion Monitor",
    namespace: "karatecarter",
    author: "Daniel Carter",
    description: "iCamera Monitor SmartApp",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@3x.png")


preferences {
    input "numCameras", "number", title:"Number of cameras to manage", defaultValue:1
    section("Motion Detection Settings", hidden: false, hideable: true) {
        input("recipients", "contact", title: "Send notifications to (optional)", multiple: true, required: false) {
            paragraph "You can enter multiple phone numbers to send an SMS to by separating them with a '+'. E.g. 5551234567+4447654321"
            input name: "sms", title: "Send SMS notification to (optional):", type: "phone", required: false
            input "push", "bool", title: "Send Push Notification", required: false
        }
    }
}

mappings {
    path("/:cameraId/TriggerMotion") {
        action: [
            GET: "cameraMotionCallback"
        ]
    }
}

def installed() {
    log.debug "Installed"

    initialize()
}

def updated() {
    log.debug "Updated"

    unsubscribe()
    initialize()
}

def initialize() {
	def cameras = getChildDevices()
    def addNew = false
    def start = 1
    def existingDevice
    def name
    def n = 1
    
    if (cameras) {
    	if (cameras.size < settings.numCameras)
        {
        	addNew = true
            start = cameras.size + 1
        }
    } else {
        addNew = true
    }
    
    if (addNew) {
        for (int i = start; i <= settings.numCameras; i++)
        {
        	try {
            	while(true) {
                	name = "iCamera.$n"
                	existingDevice = getChildDevice(name)
                    n++
                    if (!existingDevice) {
                    	break
                    }
                }
            	log.trace "Adding $name"
                def childDevice = addChildDevice("karatecarter", "iCamera", name, location.hubs[0].id, [name: "iCamera", label: "iCamera${settings.numCameras > 1? "$i": ""}", completedSetup: false])
            } catch (e) {
                log.error "Error creating device: ${e}"
            }
        }
    }
    
    cameras = getChildDevices()
    subscribe(cameras, "motion.active", motionDetected)
    subscribe(cameras, "switch.on", setUpCallbackURLs)
    
    setUpCallbackURLs()
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def setUpCallbackURLs(event)
{
	if (!state.accessToken)
    {
    	setupAccessToken()
    }
    
    def cameras = getChildDevices()
    cameras.each { camera ->
    	def callbackURL = apiServerUrl("/api/smartapps/installations/${app.id}/${camera.id}/TriggerMotion?access_token=${state.accessToken}") // As per the new format
        log.info "Registering $camera Motion Callback URL -> ${callbackURL}"
        try {
        	camera.setCallbackURL(URLEncoder.encode(longURLService(shortenURL(callbackURL))))   
        } catch (Exception e) {
        	log.warn "Error registering callback URL: $e"
        }
    }
}

private setupAccessToken() {
    log.trace "Creating Access Token for call back" // For security purposes each time we initialize we create a new token
    try {
        revokeAccessToken() // First kill the old tokens
        createAccessToken() // Now create a new token
    } catch (e) {
        log.error "Error creating access token, have you ENABLED OAuth in the SmartApp Settings?"
        sendPush "Error creating access token, have you ENABLED OAuth in the SmartApp Settings?"
        log.error "Error : $e"
        return
    }
}

def cameraMotionCallback() {
    log.info "Received motion detected callback for camera Id ${params.cameraId} with params -> $params"
    //setupCameraAccessToken() // We need a new URL each time it is set off otherwise it caches it and won't work the next time
    def cameras = getChildDevices()
    cameras.each { camera ->
        if (camera.id == params.cameraId) {
            log.info "Motion Detected on Camera $camera"
            
            if (camera.currentValue("switch") == "off") {
                log.warn "Camera $camera is not armed"
            }
            
            camera.triggerMotion()
        }
    }
}

def motionDetected(event)
{
	def cameras = getChildDevices()
    def camera = cameras.find { event.deviceId == it.id }
    def message = "${event.displayName} has detected motion"
    log.debug "SMS: $sms, Push: $push, Message: $message"
    sms ? sendText(sms, message) : ""
    push ? sendPush(message) : sendNotificationEvent(message)
}

private String shortenURL(longURL) {
    def params = [
        uri: 'http://tiny-url.info/api/v1/create',
        contentType: 'application/json',
        query: [apikey:'D4AG7G09FA819E00F77C', provider: 'tinyurl_com', format: 'json', url: longURL]
    ]

    try {
        httpGet(params) { response ->
            //log.trace "Request was successful, data=$response.data, status=$response.status"
            if (response.data.state == "ok") {
                log.trace "Short URL: ${response.data.shorturl}"
                log.trace "Long URL: ${response.data.longurl}"
                return response.data.shorturl
            } else {
                log.error "Error in return short URL: ${response.data}"
            }
        }
    } catch (e) {
        log.error "Error getting shortened URL: $e"
    }
}

// This service when called visits the shortURL to lengthen it and in the process activates the link
private String longURLService(shortURL) {
    def visitURL = "http://getlinkinfo.com/info?link=" + shortURL
    log.trace "Visit URL: $visitURL"
    return visitURL
}