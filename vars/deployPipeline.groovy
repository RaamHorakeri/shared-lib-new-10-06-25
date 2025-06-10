def call(String imageName, String environment, String imageTag, String branch, String repoUrl, String credentialsId, String envVarRepo) {
    node {
        def config = loadConfig(imageName, environment, branch, repoUrl, credentialsId, envVarRepo)
        def envConfig = config.services[imageName]?.environments[environment]

        if (!envConfig) {
            error("Configuration not found for service: ${imageName}, environment: ${environment}")
        }

        def imageFullName = "${imageName}:${imageTag}"

        stage('Setup Environment Variables') {
            echo "Preparing Jenkins credentials for environment variables..."
            envConfig.envVars.each { key, credId ->
                echo "Mapping credential ID '${credId}' to environment variable '${key}'"
            }
        }

        stage('Checkout') {
            echo "Checking out branch '${branch}' from repo '${repoUrl}'"
            checkoutFromGit(branch, repoUrl, credentialsId)
        }

        stage('Docker Login') {
            echo "Logging into Docker Hub using Jenkins credentials..."
            withCredentials([usernamePassword(
                credentialsId: 'dockerhub-credentials',
                usernameVariable: 'DOCKER_USER',
                passwordVariable: 'DOCKER_PASS'
            )]) {
                bat """
                echo Logging in as %DOCKER_USER%
                docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                """
            }
        }

        stage('Docker Build Image') {
            echo "Building Docker image: ${imageFullName}"
            bat "docker build --no-cache -t ${imageFullName} ."
        }

        stage('Docker Compose Deploy') {
            echo "Running docker compose with environment variables from Jenkins credentials..."

            def bindings = envConfig.envVars.collect { key, credId ->
                string(credentialsId: credId, variable: key)
            }

            withCredentials(bindings) {
                def envString = envConfig.envVars.collect { key, _ ->
                    "set ${key}=${env[key]} &&"
                }.join(' ')

                bat """
                ${envString} docker compose up -d --force-recreate
                """
            }
        }

        stage('Docker Cleanup') {
            echo "Cleaning up unused Docker images..."
            bat "docker image prune -af"
        }
    }
}
