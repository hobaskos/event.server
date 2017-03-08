(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('EventImageVote', EventImageVote);

    EventImageVote.$inject = ['$resource'];

    function EventImageVote ($resource) {
        var resourceUrl =  'api/event-image-votes/:id';

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
