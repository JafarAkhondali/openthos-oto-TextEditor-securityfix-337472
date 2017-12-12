define("ace/mode/search_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;


var SearchHighlightRules = function() {
    var find = window.findText || ".*";
    this.$rules = {
        "start" : [
            { //path
            regex: "^(\\[PATH\\])(.+?)(\\[/PATH\\])$",
            token: [
                "constant.search-path",
                "constant.language",
                "constant.search-path"
            ]
        }, {
            regex: "^(\\s*)(\\d+)(\t)",
            token : [
                "text",
                "constant.numeric",
                "text"
            ],
            next: "keyword"
        }
        ],
        "keyword" : [
            {
                caseInsensitive: true,
                regex: find,
                token: "keyword"
            },
            {
                regex: "$",
                next: "start"
            }
        ]
    };
};
oop.inherits(SearchHighlightRules, TextHighlightRules);

exports.SearchHighlightRules = SearchHighlightRules;
});

define("ace/mode/search",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/search_highlight_rules"], function(require, exports, module) {
"use strict";

var oop = require("../lib/oop");
var TextMode = require("./text").Mode;

var SearchHighlightRules = require("./search_highlight_rules").SearchHighlightRules;

var Mode = function() {
    this.HighlightRules = SearchHighlightRules;

    this.$behaviour = this.$defaultBehaviour;
};
oop.inherits(Mode, TextMode);

(function() {
    this.type = "text";

    this.$id = "ace/mode/search";
}).call(Mode.prototype);

exports.Mode = Mode;
});
