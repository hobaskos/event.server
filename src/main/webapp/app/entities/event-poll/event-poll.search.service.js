(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('EventPollSearch', EventPollSearch);

    EventPollSearch.$inject = ['$resource'];

    function EventPollSearch($resource) {
        var resourceUrl =  'api/_search/event-polls/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
