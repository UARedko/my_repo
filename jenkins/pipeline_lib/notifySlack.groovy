/**
 * Slack notify per user
 */

def call(Map tplParams) {
  script {
    wrap([$class: 'BuildUser']) {
      
      def CHANNEL_NOTIFY_BRANCH
      if (tplParams.CHANNEL_NOTIFY_BRANCH) {CHANNEL_NOTIFY_BRANCH = tplParams.CHANNEL_NOTIFY_BRANCH}
      	
      def notifyChannelsID = ['visvbondarenko':                         'UBEBX1X8X',
                              'vladyslav.bondarenko@intellectsoft.net': 'UBEBX1X8X',
                              'vbondarenko@gmail.com':                  'UBEBX1X8X',
                              //'yevhenii.rechun@intellectsoft.net':      'UA0CQ84B0',
                              //'Yevhenii Rechun':                        'UA0CQ84B0',
                              //'mobius4think@gmail.com':                 'UA0CQ84B0',
                              //'iryna.gaivoronska@intellectsoft.net':    'UB0SRPYJX',
                              //'tjorri':                                 'UB8V99UTB',
                              //'mike.mcclintock@thetrackr.com':          'U95L3G62D',
                              //'tuomo@longbridge.fi':                    'UB8V99UTB',
                              //'oleksandr.tatarchuk@intellectsoft.net':  'U4595B8KY',
                              //'Alex Tatarchuk ':                        'U4595B8KY',
                              //'letko-dmitry':                           'U180K6DA4',
                              //'Dmitry Letko':                           'U180K6DA4', 
                              //'dmitry.letko@intellectsoft.net':         'U180K6DA4',
                              //'pavel.shumeika@intellectsoft.net':       'UB50ERHTM',
                              'vladyslav.bondarenko':                   'UBEBX1X8X',
                              //'Andrew Kuksov':                          'U0Q37CLUE',
                              //'andrew.kuksov@intellectsoft.net':        'U0Q37CLUE',
                              //'Raman Branavitski':                      'U1A7R4JEM',
                              //'roman.branavitski@intellectsoft.net':    'U1A7R4JEM',
                              //'Dmitry Zaharov':                         'U0GHK5R2R',
                              //'dmitry.zaharov@intellectsoft.net':       'U0GHK5R2R',
                              //'Pavel Shumeika':                         'UB50ERHTM',
                              //'pavel.shumeika@intellectsoft.net':       'UB50ERHTM',
                           ]

      def BUILD_USER_NAME
      def BUILD_USER_MAIL
      def USER_ID
      def BUILD_USER = env.BUILD_USER

      try {
        BUILD_USER_NAME = sh(returnStdout: true, script: "git --no-pager show -s --format='%an'").trim().toString()
        BUILD_USER_MAIL = sh(returnStdout: true, script: "git --no-pager show -s --format='%ae'").trim().toString()
        if (!BUILD_USER) {BUILD_USER = BUILD_USER_NAME}
      } catch(Exception e) {
        println "${e}"
      }

      USER_ID = notifyChannelsID[BUILD_USER_NAME]
      if (!USER_ID) { USER_ID = notifyChannelsID[BUILD_USER_MAIL] }
      if (!USER_ID) { USER_ID = notifyChannelsID[BUILD_USER] }
      if (tplParams.USER) { USER_ID = tplParams.USER}

      println "User github info: usen_name: ${BUILD_USER_NAME}, user_email: ${BUILD_USER_MAIL}, BUILD_USER: ${BUILD_USER}"
      
      def STATUS = 'Failed'
      if (tplParams.STATUS) { STATUS = tplParams.STATUS }
      def MSG = "${STATUS} job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})"
      if (tplParams.CUSTOM_MSG) {MSG = tplParams.CUSTOM_MSG + MSG}

      def COLOR
      switch(STATUS) {
        case 'success': COLOR = '#00FF00'; break
        case 'failure': COLOR = '#FF0000'; break
        case 'notify':  COLOR = '#e9a820'; break
       }
     
      if (USER_ID) {
        slackSend(
          channel: "@${USER_ID}",
          message: "<@${USER_ID}>" + MSG,
          color: COLOR
        )   
      } else {
        slackSend (
          message: "<@${BUILD_USER}>" + MSG,
          color: COLOR
        )
      }
      if (tplParams.CHANNEL) {
        slackSend(
          channel: "#${tplParams.CHANNEL}",
          message: MSG,
          color: COLOR
        )         
      }
    }
  }
}
