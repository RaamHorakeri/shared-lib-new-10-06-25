def call(String imageName, String environment, String branch, String repoUrl, String credentialsId, String envVarRepo) {
    // Predefined common environment credential mappings per envVarRepo
    def commonEnvVarSets = [
        private: [
            SENDGRID_KEY: 'SENDGRID_KEY_DEV',
            MONGO_CONNECTION_STRING: 'MONGO_CONNECTION_STRING_DEV',
            BIFROST_ACCOUNT_PROFILE_API: 'BIFROST_ACCOUNT_PROFILE_API_DEV',
            ODIN_SECRET: 'ODIN_SECRET_DEV',
            ODIN_HOST: 'ODIN_HOST_DEV'
        ],
        public: [
            SENDGRID_KEY: 'SENDGRID_KEY_PUBLIC',
            MONGO_CONNECTION_STRING: 'MONGO_CONNECTION_STRING_PUBLIC',
            BIFROST_ACCOUNT_PROFILE_API: 'BIFROST_ACCOUNT_PROFILE_API_PUBLIC',
            ODIN_SECRET: 'ODIN_SECRET_PUBLIC',
            ODIN_HOST: 'ODIN_HOST_PUBLIC'
        ]
        // Add more sets here if needed
    ]

    def envVars = commonEnvVarSets.get(envVarRepo)

    if (!envVars) {
        error("Invalid envVarRepo: ${envVarRepo}. Valid options: ${commonEnvVarSets.keySet().join(', ')}")
    }

    return [
        services: [
            (imageName): [
                environments: [
                    (environment): [
                        agentName: '',
                        repoUrl: repoUrl,
                        branch: branch,
                        credentialsId: credentialsId,
                        envVars: envVars
                    ]
                ]
            ]
        ]
    ]
}
