/**
 * Slack notify per user
 */

def call(Map tplParams) {
  script {
    try {
    def releaseNotesHead = "Release Notes:\n"
    def rN = ""
    //= "No release notes was generated. Please check Jenkins job."    
    
    println "generateReleaseNotes"

    def lastSuccessCommit = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT
    def currCommit        = env.GIT_COMMIT
    def latestCommits     = releaseNotesHead + "5 latest commits:\n" + sh(returnStdout: true, script: "git log -5 --pretty=format:'user: %an; CommitMessage: %s; Date: %aD'").trim().toString()
    
    sh "env"

    if (lastSuccessCommit == currCommit) {
      println "No changes from last build found!"
      env.GIT_RELEASE_NOTES = latestCommits
      return
    }

    if (lastSuccessCommit) {
        releaseNotesCommits = sh(returnStdout: true, script: "git log $GIT_PREVIOUS_SUCCESSFUL_COMMIT.. --pretty=format:'user: %an; CommitMessage: %s; Date: %aD'").trim().toString()
        rN = releaseNotesHead + releaseNotesCommits
        env.GIT_RELEASE_NOTES = rN
    } else {
      println "Failed to generate release notes based on latest build. Reason: empty var GIT_PREVIOUS_SUCCESSFUL_COMMIT"
      env.GIT_RELEASE_NOTES = latestCommits
      return
    }
  } catch (Exception e) {
    println "Failed to generate release notes! Reason ${e.message}"
    rN = "Failed to generate release notes! Reason ${e.message}"
    env.GIT_RELEASE_NOTES = rN
  }
  }
}
