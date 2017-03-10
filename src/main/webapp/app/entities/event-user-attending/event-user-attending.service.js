(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('EventUserAttending', EventUserAttending);

    EventUserAttending.$inject = ['$resource', 'DateUtils'];

    function EventUserAttending ($resource, DateUtils) {
        var resourceUrl =  'api/event-user-attendings/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.createdDate = DateUtils.convertDateTimeFromServer(data.createdDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
