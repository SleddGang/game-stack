/**
* JetBrains Space Automation
* This Kotlin-script file lets you automate build activities
* For more info, see https://www.jetbrains.com/help/space/automation.html
*/

job("Upload docs") {
    container(displayName = "Say Hello", image = "ubuntu") {
        env["KEY"] = Params("docs-rsa")
        shellScript {
            content = """
                echo Uploading swagger.yaml
                ssh-add - <<< "$KEY"
                sftp ec2-user@ec2-3-212-113-177.compute-1.amazonaws.com:/var/www/docs.yourgamestack.com/html/rest <<EOF
				put ./docs/swagger.yaml
				exit
				EOF
--
            """
        }
}
