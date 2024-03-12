layout 'layout.gtpl',
        title: title,
        msg: msg,
        bodyContents: contents {

            div(class: 'container') {
                h1("Book Information" )
                includeGroovy '_book_form.gtpl'
            }
        }