/**
 * Created by okdos on 2017/5/8.
 */

define(['exports', 'when.min'], function(exports, when){

    var templateCache = {};

    function getContent(name){
        return Vue.http.get(name + ".html").then(function(res){
            return res.body;
        });
    }

    exports.config = function(config){
        path = config.path || '';
    };


    exports.require = function(path, list, callback){
        var path = path || '';

        var taskList = list.map(function(name){
            if(templateCache[path + name]){
                return templateCache[path + name];
            } else {
                return templateCache[path + name] = getContent(path + name);
            }
        });

        when.all(taskList).then(function(items){
            callback.apply(null, items);
        });
    };

});