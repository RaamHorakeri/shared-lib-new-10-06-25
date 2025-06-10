def call(String imageName, String environment, String branch, String repoUrl, String credentialsId, String envVarRepo) {
    // Case-insensitive environment mapping with empty private
    def commonEnvVarSets = [
        payment: [
            MONGO_CONNECTION_STRING: 'MONGO_CONNECTION_STRING',
            BIFROST_ACCOUNT_PROFILE_API: 'BIFROST_ACCOUNT_PROFILE_API',
            ODIN_SECRET: 'ODIN_SECRET',
            ODIN_HOST: 'ODIN_HOST'
        ],
        public: [:],
        private: [:]  // Explicit empty map
    ]

    // Normalize input and find matching key (case-insensitive)
    def normalizedInput = envVarRepo?.trim()?.toLowerCase()
    def matchedKey = commonEnvVarSets.keySet().find { it.toLowerCase() == normalizedInput }

    if (!matchedKey) {
        error("Invalid envVarRepo: '${envVarRepo}'. Valid options: ${commonEnvVarSets.keySet().join(', ')}")
    }

    // Will return empty map for 'private'
    def envVars = commonEnvVarSets[matchedKey] ?: [:]

    return [
        services: [
            (imageName): [
                environments: [
                    (environment): [
                        agentName: '',
                        repoUrl: repoUrl,
                        branch: branch,
                        credentialsId: credentialsId,
                        envVars: envVars  // Empty for private
                    ]
                ]
            ]
        ]
    ]
}
