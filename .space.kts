/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Upload docs") {
    container(displayName = "Upload Docs", image = "ubuntu") {
        env["KEY"] = Secrets("docs-rsa")
        shellScript {
            interpreter = "/bin/bash"
            content = """
            	echo Install scp
                apt-get update
                apt-get install -y openssh-client
                echo Uploading swagger.yaml
                echo "${'$'}KEY" | tr "_" "\n" > key.pem
                scp -i ./key.pem ./docs/swagger.yaml ec2-user@ec2-3-212-113-177.compute-1.amazonaws.com:/var/www/docs.yourgamestack.com/html/rest
            """
        }
    }
}
