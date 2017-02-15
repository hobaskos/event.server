(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('EventCategory', EventCategory);

    EventCategory.$inject = ['$resource'];

    function EventCategory ($resource) {
        var resourceUrl =  'api/event-categories/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
