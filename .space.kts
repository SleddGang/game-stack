/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Upload docs") {
    container(displayName = "Upload Docs", image = "ubuntu") {
        startOn {
            gitPush {
            	branchFilter {
                	+"refs/heads/release-0.1"
            	}
                pathFilter {
                    +"doc/**
                }
        	}
		}
        env["KEY"] = Secrets("docs-rsa")
        shellScript {
            interpreter = "/bin/bash"
            content = """
            	echo Installing sftp
            	apt-get update
                apt-get -y install openssh-client
                echo Uploading swagger.yaml
                echo "${'$'}KEY" | tr "_" "\n" > key.pem
                chmod 0600 key.pem
                #scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i ./key.pem /mnt/space/work/game-stack/doc/swagger.yaml ec2-user@ec2-3-212-113-177.compute-1.amazonaws.com:/var/www/docs.yourgamestack.com/html/rest
                sftp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -i key.pem sftp_user@ec2-3-212-113-177.compute-1.amazonaws.com:/var/www/docs.yourgamestack.com/html/rest <<EOF
				put /mnt/space/work/game-stack/doc/swagger.yaml
				exit
                EOF
            """
        }
    }
}
