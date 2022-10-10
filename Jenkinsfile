pipeline {
  agent any
  stages {
    stage('Fetch code') {
      agent any
      steps {
        git(url: 'https://github.com/saviola1127/RecyclerViewDemo.git', branch: 'main')
        sh 'sh "./gradle assembleDebug"'
      }
    }

  }
}