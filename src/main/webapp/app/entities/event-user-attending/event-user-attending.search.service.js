(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('EventUserAttendingSearch', EventUserAttendingSearch);

    EventUserAttendingSearch.$inject = ['$resource'];

    function EventUserAttendingSearch($resource) {
        var resourceUrl =  'api/_search/event-user-attendings/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
