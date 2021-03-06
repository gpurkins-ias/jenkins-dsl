freeStyleJob('k8s_aks_dns_ingress') {
    displayName('k8s-aks-dns-ingress')
    description('Build Dockerfiles for k8s-aks-dns-ingress.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/k8s-aks-dns-ingress')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/k8s-aks-dns-ingress.git')
                credentials('k8s-aks-dns-ingress-deploy-key')
            }
            branches('*/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
        githubPush()
    }

    wrappers { colorizeOutput() }

    environmentVariables(DOCKER_CONTENT_TRUST: '1')
    steps {
        shell('docker build --rm --force-rm -t r.j3ss.co/k8s-aks-dns-ingress:latest .')
        shell('docker push --disable-content-trust=false r.j3ss.co/k8s-aks-dns-ingress:latest')
        shell('docker rm $(docker ps --filter status=exited -q 2>/dev/null) 2> /dev/null || true')
        shell('docker rmi $(docker images --filter dangling=true -q 2>/dev/null) 2> /dev/null || true')
    }

    publishers {
        retryBuild {
            retryLimit(2)
            fixedDelay(15)
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
