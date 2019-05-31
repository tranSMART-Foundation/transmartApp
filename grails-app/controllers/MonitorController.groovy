class MonitorController {

     def monitorService

     def index =  {
        def dbStatusText = ""
        def otherStatusText = monitorService.otherStatusText

         try {
            dbStatusText = monitorService.getDatabaseStatusText(false, "")
        } catch (e) {
            dbStatusText = monitorService.getDatabaseStatusText(true, e.getMessage())
        }

         render(text: dbStatusText + otherStatusText)

     }

 }
