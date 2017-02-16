(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventCategoryDeleteController',EventCategoryDeleteController);

    EventCategoryDeleteController.$inject = ['$uibModalInstance', 'entity', 'EventCategory'];

    function EventCategoryDeleteController($uibModalInstance, entity, EventCategory) {
        var vm = this;

        vm.eventCategory = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EventCategory.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
