def l=namespace(lib.LayoutTagLib)

l.layout {
    l.main_panel {
        h1 "Accessing Jenkow Workflows"

        p {
            raw _("txt",app.rootUrl)
        }
        pre {
            def url = "${app.rootUrl}jenkow-repository.git"
            raw "git clone ${url}"

            if (my.sshd.actualPort>0) {
                raw "\ngit clone ssh://${new URL(app.rootUrl).host}:${my.sshd.actualPort}/jenkow-repository.git"
            }
        }
    }
}
