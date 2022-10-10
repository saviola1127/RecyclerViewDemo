pipeline {
  agent any
  stages {
    stage('fetch') {
      agent any
      steps {
        git(url: 'https://github.com/saviola1127/RecyclerViewDemo.git', branch: 'main')
      }
    }

      stage('build') {
      agent any
      steps {
        git(url: 'https://github.com/saviola1127/RecyclerViewDemo.git', branch: 'main')
        sh "./gradle assembleDebug"
      }
    }
  }
}
