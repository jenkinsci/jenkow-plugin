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

        h1 "Jenkow Eclipse Update Site"

        p {
            raw _("jeus",app.rootUrl)
        }
        pre {
            raw "${app.rootUrl}plugin/jenkow-plugin/eclipse.site"
        }
    }
}
