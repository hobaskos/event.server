(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('EventCategorySearch', EventCategorySearch);

    EventCategorySearch.$inject = ['$resource'];

    function EventCategorySearch($resource) {
        var resourceUrl =  'api/_search/event-categories/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
