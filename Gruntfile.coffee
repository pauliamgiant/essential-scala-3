#global module:false

"use strict"

ebook = require 'underscore-ebook-template'

module.exports = (grunt) ->
  ebook(grunt, {
    dir: {
      page: "target/pages",
      template: "src/template"
    },
    pandoc: {
      pdf: {
        options: {
          smart: false,
          pdfEngine: 'xelatex'
        }
      },
      html: {
        options: {
          smart: false
        }
      },
      epub: {
        options: {
          smart: false
        }
      }
    }
  })
  return
