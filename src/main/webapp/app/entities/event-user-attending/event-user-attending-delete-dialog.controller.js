(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventUserAttendingDeleteController',EventUserAttendingDeleteController);

    EventUserAttendingDeleteController.$inject = ['$uibModalInstance', 'entity', 'EventUserAttending'];

    function EventUserAttendingDeleteController($uibModalInstance, entity, EventUserAttending) {
        var vm = this;

        vm.eventUserAttending = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EventUserAttending.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
